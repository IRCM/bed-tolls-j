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

package ca.qc.ircm.bedtools.validation;

import static org.junit.Assert.fail;

import ca.qc.ircm.bedtools.test.config.NonTransactionalTestAnnotations;
import com.beust.jcommander.ParameterException;
import java.nio.file.Path;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class FileExistsValidationTest {
  private FileExistsValidation fileExistsValidation = new FileExistsValidation();
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void validate_Exists() throws Throwable {
    Path path = temporaryFolder.newFile("exists.txt").toPath();

    try {
      fileExistsValidation.validate("my-name", path.toString());
      // Success
    } catch (ParameterException e) {
      fail("Expected ParameterException");
    }
  }

  @Test
  public void validate_NotExists() {
    try {
      fileExistsValidation.validate("my-name", "notexists.txt");
      fail("Expected ParameterException");
    } catch (ParameterException e) {
      // Success
    }
  }
}
