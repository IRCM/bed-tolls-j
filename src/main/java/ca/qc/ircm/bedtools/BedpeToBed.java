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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Move annotation parameters.
 */
@Component
public class BedpeToBed {
  private static final String LINE_SEPARATOR = "\n";
  private static final String COLUMN_SEPARATOR = "\t";
  private static final String COMMENT = "#";
  @SuppressWarnings("unused")
  private static final Logger logger = LoggerFactory.getLogger(BedpeToBed.class);

  /**
   * Converts BED to BEDPE file.
   *
   * @param command
   *          conversion command
   * @throws IOException
   *           could not read or write BED
   */
  public void run(BedpeToBedCommand command) throws IOException {
    try (BufferedReader reader = command.reader(); BufferedWriter writer = command.writer()) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] columns = line.split(COLUMN_SEPARATOR, -1);
        if (columns[0].startsWith(COMMENT)) {
          writer.write(line);
          writer.write(LINE_SEPARATOR);
        } else {
          writer.write(columns[0]);
          writer.write(COLUMN_SEPARATOR);
          writer.write(columns[1]);
          writer.write(COLUMN_SEPARATOR);
          writer.write(columns[2]);
          writer.write(COLUMN_SEPARATOR);
          writer.write(columns[6]);
          writer.write(COLUMN_SEPARATOR);
          writer.write(columns[7]);
          writer.write(COLUMN_SEPARATOR);
          writer.write(columns[8]);
          writer.write(LINE_SEPARATOR);
        }
      }
    }
  }
}
