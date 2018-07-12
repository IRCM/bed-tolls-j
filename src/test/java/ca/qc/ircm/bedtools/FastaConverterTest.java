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

import ca.qc.ircm.bedtools.test.config.NonTransactionalTestAnnotations;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class FastaConverterTest {
  private static final String LINE_SEPARATOR = "\n";
  private static final int CHROMOSOME_COUNT = 21;
  private static final int FASTA_LINE_LENGTH = 80;
  private static final int CHROMOSOME_MAX_LENGTH = 1000000;
  private static final String SEPARATOR = "\t";
  private static final Charset FASTA_CHARSET = StandardCharsets.UTF_8;
  private FastaConverter fastaConverter = new FastaConverter();
  @Mock
  private FastaToSizesCommand command;
  private Map<String, Integer> sizes;
  private String content;

  private void generateFasta() throws IOException {
    Random random = new Random();
    sizes = IntStream.range(0, CHROMOSOME_COUNT).collect(() -> new HashMap<>(),
        (map, i) -> map.put("chr" + (i + 1), random.nextInt(CHROMOSOME_MAX_LENGTH)),
        (map1, map2) -> map1.putAll(map2));
    List<String> lines = IntStream.range(0, CHROMOSOME_COUNT).boxed().flatMap(i -> {
      List<String> chromLines = new ArrayList<>();
      String chromosome = "chr" + (i + 1);
      chromLines.add(">" + chromosome);
      int size = sizes.get(chromosome);
      for (int j = 0; j < size; j += FASTA_LINE_LENGTH) {
        String line = RandomStringUtils.randomAlphabetic(Math.min(size - j, FASTA_LINE_LENGTH));
        chromLines.add(line);
      }
      return chromLines.stream();
    }).collect(Collectors.toList());
    content = lines.stream().collect(Collectors.joining(LINE_SEPARATOR));
  }

  @Test
  public void toSizes() throws Throwable {
    generateFasta();
    ByteArrayOutputStream output = new ByteArrayOutputStream();

    fastaConverter.toSizes(new ByteArrayInputStream(content.getBytes(FASTA_CHARSET)), output,
        command);

    String outputAsString = output.toString(FASTA_CHARSET.name());
    String[] lines = outputAsString.split("\n");
    for (int i = 0; i < CHROMOSOME_COUNT; i += 1) {
      String[] columns = lines[i].split(SEPARATOR, -1);
      String chromosome = "chr" + (i + 1);
      assertEquals(2, columns.length);
      assertEquals(chromosome, columns[0]);
      assertEquals(String.valueOf(sizes.get(chromosome)), columns[1]);
    }
  }
}
