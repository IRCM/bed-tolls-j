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

import ca.qc.ircm.bedtools.validation.FileExistsValidation;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.PathConverter;
import com.beust.jcommander.validators.PositiveInteger;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Set annotations' size parameters.
 */
@Parameters(
    separators = " =",
    commandNames = SetAnnotationsSizeCommand.SET_ANNOTATIONS_SIZE_COMMAND,
    commandDescription = "Set annotations' size")
public class SetAnnotationsSizeCommand {
  public static final String SET_ANNOTATIONS_SIZE_COMMAND = "setannotationssize";
  private static final Charset CHARSET = StandardCharsets.UTF_8;

  @Parameter(names = { "-h", "-help", "--h", "--help" }, description = "Show help", help = true)
  public boolean help = false;
  @Parameter(
      names = { "-s", "--size" },
      description = "Annotations size",
      required = true,
      validateWith = PositiveInteger.class)
  public Integer size;
  @Parameter(
      names = { "-c", "--changeStart" },
      description = "Change start position instead of end",
      required = false)
  public boolean changeStart;
  @Parameter(
      names = { "-r", "--reverseForNegativeStrand" },
      description = "Change start position instead of end for negative strand."
          + " If --changeStart option is use, change end instead of start",
      required = false)
  public boolean reverseForNegativeStrand;
  @Parameter(
      names = { "-i", "--input" },
      description = "Input file. Defaults to system input for piping",
      converter = PathConverter.class,
      validateWith = FileExistsValidation.class)
  public Path input;
  @Parameter(
      names = { "-o", "--output" },
      description = "Output file. Defaults to system output for piping",
      converter = PathConverter.class)
  public Path output;

  /**
   * Returns input reader, falls back to <code>System.in</code>.
   *
   * @return input reader
   * @throws IOException
   *           could not created a reader for input
   */
  public BufferedReader reader() throws IOException {
    if (input != null) {
      return Files.newBufferedReader(input);
    } else {
      return new BufferedReader(new InputStreamReader(System.in, CHARSET));
    }
  }

  /**
   * Returns output writer, falls back to <code>System.out</code>.
   *
   * @return output writer
   * @throws IOException
   *           could not created a writer for output
   */
  public BufferedWriter writer() throws IOException {
    if (output != null) {
      return Files.newBufferedWriter(output);
    } else {
      return new BufferedWriter(new OutputStreamWriter(System.out, CHARSET));
    }
  }
}
