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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import ca.qc.ircm.bedtools.test.config.NonTransactionalTestAnnotations;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class BedTransformTest {
  private static final int LINE_COUNT = 1000;
  private static final int MAX_CHROMOSOME = 23;
  private static final int MAX_ANNOTATION_LENGTH = 10000;
  private static final int MAX_ANNOTATION_START = Integer.MAX_VALUE - MAX_ANNOTATION_LENGTH;
  private static final int MIN_ANNOTATION_SCORE = 300;
  private static final int MAX_ANNOTATION_SCORE = 900;
  private BedTransform bedTransform;
  @Mock
  private MoveAnnotationsCommand moveParameters;
  @Mock
  private SetAnnotationsSizeCommand setSizeParameters;
  private String content;

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    bedTransform = new BedTransform();
    bedGraphContent();
  }

  private void bedGraphContent() {
    Random random = new Random();
    content = IntStream.range(0, LINE_COUNT).mapToObj(lineNumber -> {
      int chromosome = random.nextInt(MAX_CHROMOSOME);
      int start = random.nextInt(MAX_ANNOTATION_START);
      int end = start + random.nextInt(MAX_ANNOTATION_LENGTH);
      String name = RandomStringUtils.randomAlphanumeric(10);
      int score =
          MIN_ANNOTATION_SCORE + random.nextInt(MAX_ANNOTATION_SCORE - MIN_ANNOTATION_SCORE);
      String strand = random.nextBoolean() ? "-" : "+";
      return chromosome + "\t" + start + "\t" + end + "\t" + name + "\t" + score + "\t" + strand;
    }).collect(Collectors.joining("\n"));
  }

  private void bedContent() {
    Random random = new Random();
    content = IntStream.range(0, LINE_COUNT).mapToObj(lineNumber -> {
      int chromosome = random.nextInt(MAX_CHROMOSOME);
      int start = random.nextInt(MAX_ANNOTATION_START);
      int end = start + random.nextInt(MAX_ANNOTATION_LENGTH);
      int score =
          MIN_ANNOTATION_SCORE + random.nextInt(MAX_ANNOTATION_SCORE - MIN_ANNOTATION_SCORE);
      return chromosome + "\t" + start + "\t" + end + "\t" + score;
    }).collect(Collectors.joining("\n"));
  }

  private void assertSetSizeContent(String outputContent, int size) {
    String[] outputLines = Arrays.asList(outputContent.split("\n")).stream()
        .filter(line -> !line.isEmpty()).toArray(count -> new String[count]);
    String[] lines = content.split("\n");
    assertEquals(lines.length, outputLines.length);
    for (int i = 0; i < lines.length; i++) {
      String[] columns = lines[i].split("\t", -1);
      String[] outputColumns = outputLines[i].split("\t", -1);
      assertEquals(columns.length, outputColumns.length);
      assertEquals(columns[0], outputColumns[0]);
      assertEquals(columns[1], outputColumns[1]);
      assertEquals(String.valueOf(Long.parseLong(columns[1]) + 3), outputColumns[2]);
      for (int j = 3; j < columns.length; j++) {
        assertEquals(columns[j], outputColumns[j]);
      }
    }
  }

  @Test
  public void setAnnotationsSize() throws Throwable {
    setSizeParameters.size = 3;
    when(setSizeParameters.reader()).thenReturn(new BufferedReader(new StringReader(content)));
    StringWriter writer = new StringWriter();
    when(setSizeParameters.writer()).thenReturn(new BufferedWriter(writer));
    bedTransform.setAnnotationsSize(setSizeParameters);
    assertSetSizeContent(writer.toString(), 3);
  }

  @Test
  public void setAnnotationsSize_ChangeStart() throws Throwable {
    setSizeParameters.size = 3;
    setSizeParameters.changeStart = true;
    when(setSizeParameters.reader()).thenReturn(new BufferedReader(new StringReader(content)));
    StringWriter writer = new StringWriter();
    when(setSizeParameters.writer()).thenReturn(new BufferedWriter(writer));
    bedTransform.setAnnotationsSize(setSizeParameters);
    String[] outputLines = Arrays.asList(writer.toString().split("\n")).stream()
        .filter(line -> !line.isEmpty()).toArray(count -> new String[count]);
    String[] lines = content.split("\n");
    assertEquals(lines.length, outputLines.length);
    for (int i = 0; i < lines.length; i++) {
      String[] columns = lines[i].split("\t", -1);
      String[] outputColumns = outputLines[i].split("\t", -1);
      assertEquals(columns.length, outputColumns.length);
      assertEquals(columns[0], outputColumns[0]);
      assertEquals(String.valueOf(Long.parseLong(columns[2]) - 3), outputColumns[1]);
      assertEquals(columns[2], outputColumns[2]);
      for (int j = 3; j < columns.length; j++) {
        assertEquals(columns[j], outputColumns[j]);
      }
    }
  }

  @Test
  public void setAnnotationsSize_ReverseForNegativeStrand() throws Throwable {
    setSizeParameters.size = 3;
    setSizeParameters.reverseForNegativeStrand = true;
    when(setSizeParameters.reader()).thenReturn(new BufferedReader(new StringReader(content)));
    StringWriter writer = new StringWriter();
    when(setSizeParameters.writer()).thenReturn(new BufferedWriter(writer));
    bedTransform.setAnnotationsSize(setSizeParameters);
    String[] outputLines = Arrays.asList(writer.toString().split("\n")).stream()
        .filter(line -> !line.isEmpty()).toArray(count -> new String[count]);
    String[] lines = content.split("\n");
    assertEquals(lines.length, outputLines.length);
    for (int i = 0; i < lines.length; i++) {
      String[] columns = lines[i].split("\t", -1);
      String[] outputColumns = outputLines[i].split("\t", -1);
      assertEquals(columns.length, outputColumns.length);
      assertEquals(columns[0], outputColumns[0]);
      if (columns[5].equals("+")) {
        assertEquals(columns[1], outputColumns[1]);
        assertEquals(String.valueOf(Long.parseLong(columns[1]) + 3), outputColumns[2]);
      } else {
        assertEquals(String.valueOf(Long.parseLong(columns[2]) - 3), outputColumns[1]);
        assertEquals(columns[2], outputColumns[2]);
      }
      for (int j = 3; j < columns.length; j++) {
        assertEquals(columns[j], outputColumns[j]);
      }
    }
  }

  @Test
  public void setAnnotationsSize_ReverseForNegativeStrand_NoStrand() throws Throwable {
    bedContent();
    setSizeParameters.size = 3;
    setSizeParameters.reverseForNegativeStrand = true;
    when(setSizeParameters.reader()).thenReturn(new BufferedReader(new StringReader(content)));
    StringWriter writer = new StringWriter();
    when(setSizeParameters.writer()).thenReturn(new BufferedWriter(writer));
    bedTransform.setAnnotationsSize(setSizeParameters);
    String[] outputLines = Arrays.asList(writer.toString().split("\n")).stream()
        .filter(line -> !line.isEmpty()).toArray(count -> new String[count]);
    String[] lines = content.split("\n");
    assertEquals(lines.length, outputLines.length);
    for (int i = 0; i < lines.length; i++) {
      String[] columns = lines[i].split("\t", -1);
      String[] outputColumns = outputLines[i].split("\t", -1);
      assertEquals(columns.length, outputColumns.length);
      assertEquals(columns[0], outputColumns[0]);
      assertEquals(columns[1], outputColumns[1]);
      assertEquals(String.valueOf(Long.parseLong(columns[1]) + 3), outputColumns[2]);
      for (int j = 3; j < columns.length; j++) {
        assertEquals(columns[j], outputColumns[j]);
      }
    }
  }

  @Test
  public void setAnnotationsSize_ChangeStart_ReverseForNegativeStrand() throws Throwable {
    setSizeParameters.size = 3;
    setSizeParameters.changeStart = true;
    setSizeParameters.reverseForNegativeStrand = true;
    when(setSizeParameters.reader()).thenReturn(new BufferedReader(new StringReader(content)));
    StringWriter writer = new StringWriter();
    when(setSizeParameters.writer()).thenReturn(new BufferedWriter(writer));
    bedTransform.setAnnotationsSize(setSizeParameters);
    String[] outputLines = Arrays.asList(writer.toString().split("\n")).stream()
        .filter(line -> !line.isEmpty()).toArray(count -> new String[count]);
    String[] lines = content.split("\n");
    assertEquals(lines.length, outputLines.length);
    for (int i = 0; i < lines.length; i++) {
      String[] columns = lines[i].split("\t", -1);
      String[] outputColumns = outputLines[i].split("\t", -1);
      assertEquals(columns.length, outputColumns.length);
      assertEquals(columns[0], outputColumns[0]);
      if (columns[5].equals("+")) {
        assertEquals(String.valueOf(Long.parseLong(columns[2]) - 3), outputColumns[1]);
        assertEquals(columns[2], outputColumns[2]);
      } else {
        assertEquals(columns[1], outputColumns[1]);
        assertEquals(String.valueOf(Long.parseLong(columns[1]) + 3), outputColumns[2]);
      }
      for (int j = 3; j < columns.length; j++) {
        assertEquals(columns[j], outputColumns[j]);
      }
    }
  }

  @Test
  public void setAnnotationsSize_Comments() throws Throwable {
    String content = "#comment 1\n" + this.content.split("\n")[0] + "\n#comment 2\n" + Arrays
        .asList(this.content.split("\n")).stream().skip(1).collect(Collectors.joining("\n"));
    setSizeParameters.size = 3;
    when(setSizeParameters.reader()).thenReturn(new BufferedReader(new StringReader(content)));
    StringWriter writer = new StringWriter();
    when(setSizeParameters.writer()).thenReturn(new BufferedWriter(writer));
    bedTransform.setAnnotationsSize(setSizeParameters);
    List<String> outputLines = Arrays.asList(writer.toString().split("\n"));
    assertEquals("#comment 1", outputLines.get(0));
    assertEquals("#comment 2", outputLines.get(2));
    assertSetSizeContent(outputLines.stream().filter(line -> !line.startsWith("#"))
        .collect(Collectors.joining("\n")), 3);
  }

  @Test
  public void setAnnotationsSize_Track() throws Throwable {
    String content = "track name=\"my track\"\n" + this.content;
    setSizeParameters.size = 3;
    when(setSizeParameters.reader()).thenReturn(new BufferedReader(new StringReader(content)));
    StringWriter writer = new StringWriter();
    when(setSizeParameters.writer()).thenReturn(new BufferedWriter(writer));
    bedTransform.setAnnotationsSize(setSizeParameters);
    List<String> outputLines = Arrays.asList(writer.toString().split("\n"));
    assertEquals("track name=\"my track\"", outputLines.get(0));
    assertSetSizeContent(outputLines.stream().skip(1).collect(Collectors.joining("\n")), 3);
  }

  @Test
  public void setAnnotationsSize_BrowserAndTrack() throws Throwable {
    String content =
        "browser position chr7:127471196-127495720\ntrack name=\"my track\"\n" + this.content;
    setSizeParameters.size = 3;
    when(setSizeParameters.reader()).thenReturn(new BufferedReader(new StringReader(content)));
    StringWriter writer = new StringWriter();
    when(setSizeParameters.writer()).thenReturn(new BufferedWriter(writer));
    bedTransform.setAnnotationsSize(setSizeParameters);
    List<String> outputLines = Arrays.asList(writer.toString().split("\n"));
    assertEquals("browser position chr7:127471196-127495720", outputLines.get(0));
    assertEquals("track name=\"my track\"", outputLines.get(1));
    assertSetSizeContent(outputLines.stream().skip(2).collect(Collectors.joining("\n")), 3);
  }

  @Test
  public void setAnnotationsSize_BrowserAndTrackAndComment() throws Throwable {
    String content =
        "browser position chr7:127471196-127495720\ntrack name=\"my track\"\n#comment\n"
            + this.content;
    setSizeParameters.size = 3;
    when(setSizeParameters.reader()).thenReturn(new BufferedReader(new StringReader(content)));
    StringWriter writer = new StringWriter();
    when(setSizeParameters.writer()).thenReturn(new BufferedWriter(writer));
    bedTransform.setAnnotationsSize(setSizeParameters);
    List<String> outputLines = Arrays.asList(writer.toString().split("\n"));
    assertEquals("browser position chr7:127471196-127495720", outputLines.get(0));
    assertEquals("track name=\"my track\"", outputLines.get(1));
    assertEquals("#comment", outputLines.get(2));
    assertSetSizeContent(outputLines.stream().skip(3).collect(Collectors.joining("\n")), 3);
  }

  private void assertMoveContent(String outputContent, int distance) {
    String[] outputLines = Arrays.asList(outputContent.split("\n")).stream()
        .filter(line -> !line.isEmpty()).toArray(count -> new String[count]);
    String[] lines = this.content.split("\n");
    assertEquals(lines.length, outputLines.length);
    for (int i = 0; i < lines.length; i++) {
      String[] columns = lines[i].split("\t", -1);
      String[] outputColumns = outputLines[i].split("\t", -1);
      assertEquals(columns.length, outputColumns.length);
      assertEquals(columns[0], outputColumns[0]);
      assertEquals(columns[0] + ":" + columns[1],
          String.valueOf(Long.parseLong(columns[1]) + distance), outputColumns[1]);
      assertEquals(columns[0] + ":" + columns[1],
          String.valueOf(Long.parseLong(columns[2]) + distance), outputColumns[2]);
      for (int j = 3; j < columns.length; j++) {
        assertEquals(columns[j], outputColumns[j]);
      }
    }
  }

  @Test
  public void moveAnnotations() throws Throwable {
    moveParameters.distance = 3;
    when(moveParameters.reader()).thenReturn(new BufferedReader(new StringReader(content)));
    StringWriter writer = new StringWriter();
    when(moveParameters.writer()).thenReturn(new BufferedWriter(writer));
    bedTransform.moveAnnotations(moveParameters);
    assertMoveContent(writer.toString(), 3);
  }

  @Test
  public void moveAnnotations_NegativeDistance() throws Throwable {
    moveParameters.distance = -3;
    when(moveParameters.reader()).thenReturn(new BufferedReader(new StringReader(content)));
    StringWriter writer = new StringWriter();
    when(moveParameters.writer()).thenReturn(new BufferedWriter(writer));
    bedTransform.moveAnnotations(moveParameters);
    assertMoveContent(writer.toString(), -3);
  }

  @Test
  public void moveAnnotations_ReverseForNegativeStrand() throws Throwable {
    moveParameters.distance = 3;
    moveParameters.reverseForNegativeStrand = true;
    when(moveParameters.reader()).thenReturn(new BufferedReader(new StringReader(content)));
    StringWriter writer = new StringWriter();
    when(moveParameters.writer()).thenReturn(new BufferedWriter(writer));
    bedTransform.moveAnnotations(moveParameters);
    String[] outputLines = Arrays.asList(writer.toString().split("\n")).stream()
        .filter(line -> !line.isEmpty()).toArray(count -> new String[count]);
    String[] lines = this.content.split("\n");
    assertEquals(lines.length, outputLines.length);
    for (int i = 0; i < lines.length; i++) {
      String[] columns = lines[i].split("\t", -1);
      String[] outputColumns = outputLines[i].split("\t", -1);
      assertEquals(columns.length, outputColumns.length);
      assertEquals(columns[0], outputColumns[0]);
      if (columns[5].equals("+")) {
        assertEquals(columns[0] + ":" + columns[1],
            String.valueOf(Long.parseLong(columns[1]) + moveParameters.distance), outputColumns[1]);
        assertEquals(columns[0] + ":" + columns[1],
            String.valueOf(Long.parseLong(columns[2]) + moveParameters.distance), outputColumns[2]);
      } else {
        assertEquals(columns[0] + ":" + columns[1],
            String.valueOf(Long.parseLong(columns[1]) - moveParameters.distance), outputColumns[1]);
        assertEquals(columns[0] + ":" + columns[1],
            String.valueOf(Long.parseLong(columns[2]) - moveParameters.distance), outputColumns[2]);
      }
      for (int j = 3; j < columns.length; j++) {
        assertEquals(columns[j], outputColumns[j]);
      }
    }
  }

  @Test
  public void moveAnnotations_ReverseForNegativeStrand_NoStrand() throws Throwable {
    bedContent();
    moveParameters.distance = 3;
    moveParameters.reverseForNegativeStrand = true;
    when(moveParameters.reader()).thenReturn(new BufferedReader(new StringReader(content)));
    StringWriter writer = new StringWriter();
    when(moveParameters.writer()).thenReturn(new BufferedWriter(writer));
    bedTransform.moveAnnotations(moveParameters);
    assertMoveContent(writer.toString(), 3);
  }

  @Test
  public void moveAnnotations_NegativeDistanceReverseForNegativeStrand() throws Throwable {
    moveParameters.distance = -3;
    moveParameters.reverseForNegativeStrand = true;
    when(moveParameters.reader()).thenReturn(new BufferedReader(new StringReader(content)));
    StringWriter writer = new StringWriter();
    when(moveParameters.writer()).thenReturn(new BufferedWriter(writer));
    bedTransform.moveAnnotations(moveParameters);
    String[] outputLines = Arrays.asList(writer.toString().split("\n")).stream()
        .filter(line -> !line.isEmpty()).toArray(count -> new String[count]);
    String[] lines = this.content.split("\n");
    assertEquals(lines.length, outputLines.length);
    for (int i = 0; i < lines.length; i++) {
      String[] columns = lines[i].split("\t", -1);
      String[] outputColumns = outputLines[i].split("\t", -1);
      assertEquals(columns.length, outputColumns.length);
      assertEquals(columns[0], outputColumns[0]);
      if (columns[5].equals("+")) {
        assertEquals(columns[0] + ":" + columns[1],
            String.valueOf(Long.parseLong(columns[1]) + moveParameters.distance), outputColumns[1]);
        assertEquals(columns[0] + ":" + columns[1],
            String.valueOf(Long.parseLong(columns[2]) + moveParameters.distance), outputColumns[2]);
      } else {
        assertEquals(columns[0] + ":" + columns[1],
            String.valueOf(Long.parseLong(columns[1]) - moveParameters.distance), outputColumns[1]);
        assertEquals(columns[0] + ":" + columns[1],
            String.valueOf(Long.parseLong(columns[2]) - moveParameters.distance), outputColumns[2]);
      }
      for (int j = 3; j < columns.length; j++) {
        assertEquals(columns[j], outputColumns[j]);
      }
    }
  }

  @Test
  public void moveAnnotations_DiscardNegative() throws Throwable {
    content = "chr1\t0\t10\ttest_negative\t300\t+\n" + content;
    moveParameters.distance = -3;
    moveParameters.discardNegative = true;
    when(moveParameters.reader()).thenReturn(new BufferedReader(new StringReader(content)));
    StringWriter writer = new StringWriter();
    when(moveParameters.writer()).thenReturn(new BufferedWriter(writer));
    bedTransform.moveAnnotations(moveParameters);
    String[] outputLines = Arrays.asList(writer.toString().split("\n")).stream()
        .filter(line -> !line.isEmpty()).toArray(count -> new String[count]);
    String[] lines = this.content.split("\n");
    assertTrue(lines.length + " > " + outputLines.length, lines.length > outputLines.length);
    int outputIndex = 0;
    for (int i = 0; i < lines.length; i++) {
      String[] columns = lines[i].split("\t", -1);
      String[] outputColumns = outputLines[outputIndex++].split("\t", -1);
      if (Integer.valueOf(columns[1]) < -moveParameters.distance
          || Integer.valueOf(columns[2]) < -moveParameters.distance) {
        outputIndex--;
        continue;
      }
      assertEquals(columns.length, outputColumns.length);
      assertEquals(columns[0], outputColumns[0]);
      assertEquals(columns[0] + ":" + columns[1],
          String.valueOf(Long.parseLong(columns[1]) + moveParameters.distance), outputColumns[1]);
      assertEquals(columns[0] + ":" + columns[1],
          String.valueOf(Long.parseLong(columns[2]) + moveParameters.distance), outputColumns[2]);
      for (int j = 3; j < columns.length; j++) {
        assertEquals(columns[j], outputColumns[j]);
      }
    }
    assertEquals(outputIndex, outputLines.length);
  }

  @Test
  public void moveAnnotations_Comments() throws Throwable {
    String content = "#comment 1\n" + this.content.split("\n")[0] + "\n#comment 2\n" + Arrays
        .asList(this.content.split("\n")).stream().skip(1).collect(Collectors.joining("\n"));
    moveParameters.distance = 3;
    when(moveParameters.reader()).thenReturn(new BufferedReader(new StringReader(content)));
    StringWriter writer = new StringWriter();
    when(moveParameters.writer()).thenReturn(new BufferedWriter(writer));
    bedTransform.moveAnnotations(moveParameters);
    List<String> lines = Arrays.asList(writer.toString().split("\n"));
    assertEquals("#comment 1", lines.get(0));
    assertEquals("#comment 2", lines.get(2));
    assertMoveContent(
        lines.stream().filter(line -> !line.startsWith("#")).collect(Collectors.joining("\n")), 3);
  }

  @Test
  public void moveAnnotations_Track() throws Throwable {
    String content = "track name=\"my track\"\n" + this.content;
    moveParameters.distance = 3;
    when(moveParameters.reader()).thenReturn(new BufferedReader(new StringReader(content)));
    StringWriter writer = new StringWriter();
    when(moveParameters.writer()).thenReturn(new BufferedWriter(writer));
    bedTransform.moveAnnotations(moveParameters);
    List<String> lines = Arrays.asList(writer.toString().split("\n"));
    assertEquals("track name=\"my track\"", lines.get(0));
    assertMoveContent(lines.stream().skip(1).collect(Collectors.joining("\n")), 3);
  }

  @Test
  public void moveAnnotations_BrowserAndTrack() throws Throwable {
    String content =
        "browser position chr7:127471196-127495720\ntrack name=\"my track\"\n" + this.content;
    moveParameters.distance = 3;
    when(moveParameters.reader()).thenReturn(new BufferedReader(new StringReader(content)));
    StringWriter writer = new StringWriter();
    when(moveParameters.writer()).thenReturn(new BufferedWriter(writer));
    bedTransform.moveAnnotations(moveParameters);
    List<String> lines = Arrays.asList(writer.toString().split("\n"));
    assertEquals("browser position chr7:127471196-127495720", lines.get(0));
    assertEquals("track name=\"my track\"", lines.get(1));
    assertMoveContent(lines.stream().skip(2).collect(Collectors.joining("\n")), 3);
  }

  @Test
  public void moveAnnotations_BrowserAndTrackAndComment() throws Throwable {
    String content =
        "browser position chr7:127471196-127495720\ntrack name=\"my track\"\n#comment\n"
            + this.content;
    moveParameters.distance = 3;
    when(moveParameters.reader()).thenReturn(new BufferedReader(new StringReader(content)));
    StringWriter writer = new StringWriter();
    when(moveParameters.writer()).thenReturn(new BufferedWriter(writer));
    bedTransform.moveAnnotations(moveParameters);
    List<String> lines = Arrays.asList(writer.toString().split("\n"));
    assertEquals("browser position chr7:127471196-127495720", lines.get(0));
    assertEquals("track name=\"my track\"", lines.get(1));
    assertEquals("#comment", lines.get(2));
    assertMoveContent(lines.stream().skip(3).collect(Collectors.joining("\n")), 3);
  }
}
