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
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Filter BEDPE.
 */
@Component
public class FilterBedpe {
  private static final String LINE_SEPARATOR = "\n";
  private static final String COLUMN_SEPARATOR = "\t";
  private static final String COMMENT = "#";
  @SuppressWarnings("unused")
  private static final Logger logger = LoggerFactory.getLogger(FilterBedpe.class);

  /**
   * Filter BEDPE file.
   *
   * @param command
   *          filter BEDPE command
   * @throws IOException
   *           could not read or write BEDPE
   */
  public void run(FilterBedpeCommand command) throws IOException {
    Predicate<String> predicate = line -> true;
    if (command.maximumInsertSize != null) {
      predicate = predicate.and(maximumInsertSizeFilter(command.maximumInsertSize));
    }
    long count = 0;
    try (BufferedReader reader = command.reader(); BufferedWriter writer = command.writer()) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] columns = line.split(COLUMN_SEPARATOR, -1);
        if (columns[0].startsWith(COMMENT)) {
          writer.write(line);
          writer.write(LINE_SEPARATOR);
        } else {
          if (predicate.test(line)) {
            writer.write(line);
            writer.write(LINE_SEPARATOR);
          } else {
            count++;
          }
        }
      }
    }
    logger.info("removed {} lines from file", count);
  }

  private Predicate<String> maximumInsertSizeFilter(int maximumInsertSize) {
    return line -> {
      String[] columns = line.split(COLUMN_SEPARATOR);
      long start1 = Long.parseLong(columns[1]);
      long end1 = Long.parseLong(columns[2]);
      long start2 = Long.parseLong(columns[4]);
      long end2 = Long.parseLong(columns[5]);
      if (start1 > start2) {
        return start1 - end2 <= maximumInsertSize;
      } else {
        return start2 - end1 <= maximumInsertSize;
      }
    };
  }
}
