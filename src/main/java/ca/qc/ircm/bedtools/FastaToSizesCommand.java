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

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/**
 * Fasta to sizes conversion.
 */
@Parameters(
    separators = " =",
    commandNames = FastaToSizesCommand.FASTA_TO_SIZES_COMMAND,
    commandDescription = "Converts FASTA to sizes")
public class FastaToSizesCommand {
  public static final String FASTA_TO_SIZES_COMMAND = "fasta2sizes";

  @Parameter(names = { "-h", "-help", "--h", "--help" }, description = "Show help", help = true)
  public boolean help = false;
}
