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
import org.springframework.stereotype.Component;

/**
 * Converts FASTA to other formats.
 */
@Component
public class FastaConverter {
  private static final String LINE_SEPARATOR = "\n";
  private static final String SEPARATOR = "\t";

  /**
   * Converts FASTA file to sizes file.
   *
   * @param parameters
   *          parameters
   * @throws IOException
   *           could not read or write BED
   */
  public void toSizes(FastaToSizesCommand parameters) throws IOException {
    try (BufferedReader reader = parameters.reader(); BufferedWriter writer = parameters.writer()) {
      String line;
      String chromosome = null;
      long size = 0;
      while ((line = reader.readLine()) != null) {
        if (line.startsWith(">")) {
          if (chromosome != null) {
            writeSize(writer, chromosome, size);
          }
          chromosome = line.substring(1);
          size = 0;
        } else {
          size += line.length();
        }
      }
      if (chromosome != null) {
        writeSize(writer, chromosome, size);
      }
    }
  }

  private void writeSize(BufferedWriter writer, String chromosome, long size) throws IOException {
    writer.write(chromosome);
    writer.write(SEPARATOR);
    writer.write(String.valueOf(size));
    writer.write(LINE_SEPARATOR);
  }
}
