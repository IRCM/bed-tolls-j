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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import javax.inject.Inject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class BedpeToBedTest {
  @Inject
  private BedpeToBed service;
  private BedpeToBedCommand command = new BedpeToBedCommand();
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void run() throws Throwable {
    command.input = Paths.get(getClass().getResource("/test.bedpe").toURI());
    command.output = temporaryFolder.newFile("output.bed").toPath();
    service.run(command);
    List<String> outputLines = Files.readAllLines(command.output);
    List<String> lines = Files.readAllLines(command.input);
    assertEquals(lines.size(), outputLines.size());
    for (int i = 0; i < lines.size(); i++) {
      String[] columns = lines.get(i).split("\t", -1);
      String[] outputColumns = outputLines.get(i).split("\t", -1);
      assertEquals(columns[0], outputColumns[0]);
      assertEquals(columns[1], outputColumns[1]);
      assertEquals(columns[2], outputColumns[2]);
      assertEquals(columns[6], outputColumns[3]);
      assertEquals(columns[7], outputColumns[4]);
      assertEquals(columns[8], outputColumns[5]);
    }
  }

  @Test
  public void run_Comment() throws Throwable {
    command.input = Paths.get(getClass().getResource("/test.bedpe").toURI());
    command.output = temporaryFolder.newFile("output.bed").toPath();
    service.run(command);
    List<String> outputLines = Files.readAllLines(command.output);
    List<String> lines = Files.readAllLines(command.input);
    assertEquals(lines.size(), outputLines.size());
    for (int i = 0; i < lines.size(); i++) {
      if (lines.get(i).startsWith("#")) {
        assertEquals(lines.get(i), outputLines.get(i));
      } else {
        String[] columns = lines.get(i).split("\t", -1);
        String[] outputColumns = outputLines.get(i).split("\t", -1);
        assertEquals(columns[0], outputColumns[0]);
        assertEquals(columns[1], outputColumns[1]);
        assertEquals(columns[2], outputColumns[2]);
        assertEquals(columns[6], outputColumns[3]);
        assertEquals(columns[7], outputColumns[4]);
        assertEquals(columns[8], outputColumns[5]);
      }
    }
  }
}
