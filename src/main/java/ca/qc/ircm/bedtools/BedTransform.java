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

import ca.qc.ircm.bedtools.io.ChunkReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * BED file transformations.
 */
@Component
public class BedTransform {
  private static final String LINE_SEPARATOR = "\n";
  private static final String COLUMN_SEPARATOR = "\t";
  private static final String BROWSER_PATTERN = "^browser( .*)?$";
  private static final String TRACK_PATTERN = "^track( .*)?$";
  private static final String NEGATIVE_STRAND = "-";
  private static final String COMMENT = "#";
  private static final Charset BED_CHARSET = StandardCharsets.UTF_8;

  /**
   * Sets the size of annotations in BED file.
   *
   * @param input
   *          BED to trim
   * @param output
   *          output
   * @param parameters
   *          size change parameters
   * @throws IOException
   *           could not read or write BED
   */
  public void setAnnotationsSize(InputStream input, OutputStream output,
      SetAnnotationsSizeCommand parameters) throws IOException {
    Pattern browserPattern = Pattern.compile(BROWSER_PATTERN);
    Pattern trackPattern = Pattern.compile(TRACK_PATTERN);
    BiFunction<String, String, String> changeStart =
        (start, end) -> String.valueOf(Long.parseLong(end) - parameters.size);
    BiFunction<String, String, String> changeEnd =
        (start, end) -> String.valueOf(Long.parseLong(start) + parameters.size);
    try (
        ChunkReader reader =
            new ChunkReader(new BufferedReader(new InputStreamReader(input, BED_CHARSET)), 1000000);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, BED_CHARSET))) {
      List<String> chunk;
      while (!(chunk = reader.readChunk()).isEmpty()) {
        for (String line : chunk) {
          String[] columns = line.split(COLUMN_SEPARATOR, -1);
          if (browserPattern.matcher(columns[0]).matches()
              || trackPattern.matcher(columns[0]).matches() || columns[0].startsWith(COMMENT)) {
            writer.write(line);
            writer.write(LINE_SEPARATOR);
          } else {
            BiFunction<String, String, String> startFunction = (start, end) -> start;
            BiFunction<String, String, String> endFunction = (start, end) -> end;
            if (parameters.reverseForNegativeStrand && columns.length >= 5
                && columns[5].equals(NEGATIVE_STRAND)) {
              if (parameters.changeStart) {
                endFunction = changeEnd;
              } else {
                startFunction = changeStart;
              }
            } else {
              if (parameters.changeStart) {
                startFunction = changeStart;
              } else {
                endFunction = changeEnd;
              }
            }
            columns[1] = startFunction.apply(columns[1], columns[2]);
            columns[2] = endFunction.apply(columns[1], columns[2]);
            writer.write(
                Arrays.asList(columns).stream().collect(Collectors.joining(COLUMN_SEPARATOR)));
            writer.write(LINE_SEPARATOR);
          }
        }
      }
    }
  }

  /**
   * Move annotations in BED file.
   *
   * @param parameters
   *          move parameters
   * @throws IOException
   *           could not read or write BED
   */
  public void moveAnnotations(MoveAnnotationsCommand parameters) throws IOException {
    Pattern browserPattern = Pattern.compile(BROWSER_PATTERN);
    Pattern trackPattern = Pattern.compile(TRACK_PATTERN);
    try (ChunkReader reader = new ChunkReader(parameters.reader(), 1000000);
        BufferedWriter writer = parameters.writer()) {
      List<String> chunk;
      while (!(chunk = reader.readChunk()).isEmpty()) {
        for (String line : chunk) {
          String[] columns = line.split(COLUMN_SEPARATOR, -1);
          if (browserPattern.matcher(columns[0]).matches()
              || trackPattern.matcher(columns[0]).matches() || columns[0].startsWith(COMMENT)) {
            writer.write(line);
            writer.write(LINE_SEPARATOR);
          } else {
            columns[1] = String.valueOf(Long.parseLong(columns[1]) + parameters.distance);
            columns[2] = String.valueOf(Long.parseLong(columns[2]) + parameters.distance);
            writer.write(
                Arrays.asList(columns).stream().collect(Collectors.joining(COLUMN_SEPARATOR)));
            writer.write(LINE_SEPARATOR);
          }
        }
      }
    }
  }
}
