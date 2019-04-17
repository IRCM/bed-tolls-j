/*
 * Copyright (c) 2017 Institut de recherches cliniques de Montreal (IRCM)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ca.qc.ircm.bedtools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import ca.qc.ircm.bedtools.test.config.NonTransactionalTestAnnotations;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.inject.Inject;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class FilterBedpeTest {
  private static final int LINE_COUNT = 1000;
  private static final int MAX_CHROMOSOME = 23;
  private static final int MAX_ANNOTATION_LENGTH = 100;
  private static final int MAX_ANNOTATION_START = 2000000;
  private static final int MIN_ANNOTATION_SCORE = 0;
  private static final int MAX_ANNOTATION_SCORE = 200;
  private static final int MAX_ANNOTATION_INSERTSIZE = 800;
  @Inject
  private FilterBedpe service;
  @Mock
  private FilterBedpeCommand command;
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private String content;

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    bedpeContent();
  }

  private void bedpeContent() {
    Random random = new Random();
    content = IntStream.range(0, LINE_COUNT).mapToObj(lineNumber -> {
      int chromosome = random.nextInt(MAX_CHROMOSOME);
      int start = random.nextInt(MAX_ANNOTATION_START);
      int end = start + random.nextInt(MAX_ANNOTATION_LENGTH);
      String name = RandomStringUtils.randomAlphanumeric(10);
      int score =
          MIN_ANNOTATION_SCORE + random.nextInt(MAX_ANNOTATION_SCORE - MIN_ANNOTATION_SCORE);
      String strand = random.nextBoolean() ? "-" : "+";
      int chromosome2 = chromosome;
      int start2;
      int end2;
      String strand2;
      if (strand.equals("+")) {
        start2 = end + random.nextInt(MAX_ANNOTATION_INSERTSIZE);
        end2 = start2 + random.nextInt(MAX_ANNOTATION_LENGTH);
        strand2 = "-";
      } else {
        end2 = start - random.nextInt(MAX_ANNOTATION_INSERTSIZE);
        start2 = end2 - random.nextInt(MAX_ANNOTATION_LENGTH);
        strand2 = "+";
      }
      return chromosome + "\t" + start + "\t" + end + "\t" + chromosome2 + "\t" + start2 + "\t"
          + end2 + "\t" + name + "\t" + score + "\t" + strand + "\t" + strand2;
    }).collect(Collectors.joining("\n"));
  }

  @Test
  public void run() throws Throwable {
    when(command.reader()).thenReturn(new BufferedReader(new StringReader(content)));
    StringWriter writer = new StringWriter();
    when(command.writer()).thenReturn(new BufferedWriter(writer));
    service.run(command);
    String[] outputLines = Arrays.asList(writer.toString().split("\n")).stream()
        .filter(line -> !line.isEmpty()).toArray(count -> new String[count]);
    String[] lines = content.split("\n");
    assertEquals(lines.length, outputLines.length);
    for (int i = 0; i < lines.length; i++) {
      String[] columns = lines[i].split("\t", -1);
      String[] outputColumns = outputLines[i].split("\t", -1);
      for (int j = 0; j < columns.length; j++) {
        assertEquals(columns[j], outputColumns[j]);
      }
    }
  }

  @Test
  public void run_MaximumInsertSize() throws Throwable {
    command.maximumInsertSize = 400;
    when(command.reader()).thenReturn(new BufferedReader(new StringReader(content)));
    StringWriter writer = new StringWriter();
    when(command.writer()).thenReturn(new BufferedWriter(writer));
    service.run(command);
    String[] outputLines = Arrays.asList(writer.toString().split("\n")).stream()
        .filter(line -> !line.isEmpty()).toArray(count -> new String[count]);
    String[] lines = content.split("\n");
    BiFunction<Integer, Integer, Integer> computeInsertSize =
        (value1, value2) -> Math.abs(value1 - value2);
    int outputIndex = 0;
    for (int i = 0; i < lines.length; i++) {
      String[] columns = lines[i].split("\t", -1);
      int start = Integer.parseInt(columns[1]);
      int end = Integer.parseInt(columns[2]);
      int start2 = Integer.parseInt(columns[4]);
      int end2 = Integer.parseInt(columns[5]);
      int insertSize;
      if (columns[8].equals("+")) {
        insertSize = computeInsertSize.apply(end, start2);
      } else {
        insertSize = computeInsertSize.apply(end2, start);
      }
      if (insertSize <= command.maximumInsertSize) {
        if (outputIndex > outputLines.length) {
          fail(outputIndex + " is greater than the number of output lines");
        }
        String[] outputColumns = outputLines[outputIndex++].split("\t", -1);
        for (int j = 0; j < columns.length; j++) {
          assertEquals(lines[i] + " at line " + (i + 1) + " vs " + outputLines[outputIndex - 1]
              + " at line " + outputIndex, columns[j], outputColumns[j]);
        }
      }
    }
    assertEquals(outputIndex, outputLines.length);
  }

  @Test
  public void run_Comments() throws Throwable {
    String content = "#comment 1\n" + this.content.split("\n")[0] + "\n#comment 2\n" + Arrays
        .asList(this.content.split("\n")).stream().skip(1).collect(Collectors.joining("\n"));
    command.maximumInsertSize = 400;
    when(command.reader()).thenReturn(new BufferedReader(new StringReader(content)));
    StringWriter writer = new StringWriter();
    when(command.writer()).thenReturn(new BufferedWriter(writer));
    service.run(command);
    String[] outputLines = Arrays.asList(writer.toString().split("\n")).stream()
        .filter(line -> !line.isEmpty()).toArray(count -> new String[count]);
    String[] lines = content.split("\n");
    BiFunction<Integer, Integer, Integer> computeInsertSize =
        (value1, value2) -> Math.abs(value1 - value2);
    int outputIndex = 0;
    for (int i = 0; i < lines.length; i++) {
      if (lines[i].startsWith("#")) {
        assertEquals(lines[i], outputLines[outputIndex++]);
        continue;
      }
      String[] columns = lines[i].split("\t", -1);
      int start = Integer.parseInt(columns[1]);
      int end = Integer.parseInt(columns[2]);
      int start2 = Integer.parseInt(columns[4]);
      int end2 = Integer.parseInt(columns[5]);
      int insertSize;
      if (columns[8].equals("+")) {
        insertSize = computeInsertSize.apply(end, start2);
      } else {
        insertSize = computeInsertSize.apply(end2, start);
      }
      if (insertSize <= command.maximumInsertSize) {
        if (outputIndex > outputLines.length) {
          fail(outputIndex + " is greater than the number of output lines");
        }
        String[] outputColumns = outputLines[outputIndex++].split("\t", -1);
        for (int j = 0; j < columns.length; j++) {
          assertEquals(lines[i] + " at line " + (i + 1) + " vs " + outputLines[outputIndex - 1]
              + " at line " + outputIndex, columns[j], outputColumns[j]);
        }
      }
    }
    assertEquals(outputIndex, outputLines.length);
  }
}
