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

import static ca.qc.ircm.bedtools.FastaToSizesCommand.FASTA_TO_SIZES_COMMAND;
import static ca.qc.ircm.bedtools.MoveAnnotationsCommand.MOVE_ANNOTATIONS_COMMAND;
import static ca.qc.ircm.bedtools.SetAnnotationsSizeCommand.SET_ANNOTATIONS_SIZE_COMMAND;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import ca.qc.ircm.bedtools.test.config.NonTransactionalTestAnnotations;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class MainServiceTest {
  private MainService mainService;
  @Mock
  private BedTransform bedTransform;
  @Mock
  private FastaConverter fastaConverter;
  @Captor
  private ArgumentCaptor<SetAnnotationsSizeCommand> setAnnotationsSizeCommandCaptor;
  @Captor
  private ArgumentCaptor<MoveAnnotationsCommand> moveAnnotationCommandCaptor;
  @Captor
  private ArgumentCaptor<FastaToSizesCommand> fastaToSizesCommandCaptor;
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Before
  public void beforeTest() {
    mainService = new MainService(bedTransform, fastaConverter, true);
  }

  @Test
  public void run_RunnerDisabled() {
    mainService = new MainService(bedTransform, fastaConverter, false);
    mainService.run(new String[] { SET_ANNOTATIONS_SIZE_COMMAND, "-s", "1" });
    verifyZeroInteractions(bedTransform);
    verifyZeroInteractions(fastaConverter);
  }

  @Test
  public void run_Help() {
    mainService.run("-h");
    verifyZeroInteractions(bedTransform);
    verifyZeroInteractions(fastaConverter);
  }

  @Test
  public void run_SetAnnotationsSize() throws Throwable {
    mainService.run(new String[] { SET_ANNOTATIONS_SIZE_COMMAND, "-s", "1" });
    verify(bedTransform).setAnnotationsSize(setAnnotationsSizeCommandCaptor.capture());
    assertEquals((Integer) 1, setAnnotationsSizeCommandCaptor.getValue().size);
    assertEquals(false, setAnnotationsSizeCommandCaptor.getValue().changeStart);
    assertEquals(false, setAnnotationsSizeCommandCaptor.getValue().reverseForNegativeStrand);
  }

  @Test
  public void run_SetAnnotationsSize_LongName() throws Throwable {
    mainService.run(new String[] { SET_ANNOTATIONS_SIZE_COMMAND, "--size", "1" });
    verify(bedTransform).setAnnotationsSize(setAnnotationsSizeCommandCaptor.capture());
    assertEquals((Integer) 1, setAnnotationsSizeCommandCaptor.getValue().size);
    assertEquals(false, setAnnotationsSizeCommandCaptor.getValue().changeStart);
    assertEquals(false, setAnnotationsSizeCommandCaptor.getValue().reverseForNegativeStrand);
  }

  @Test
  public void run_SetAnnotationsSize_UpperCase() throws Throwable {
    mainService.run(new String[] { SET_ANNOTATIONS_SIZE_COMMAND.toUpperCase(), "-s", "1" });
    verify(bedTransform).setAnnotationsSize(setAnnotationsSizeCommandCaptor.capture());
    assertEquals((Integer) 1, setAnnotationsSizeCommandCaptor.getValue().size);
    assertEquals(false, setAnnotationsSizeCommandCaptor.getValue().changeStart);
    assertEquals(false, setAnnotationsSizeCommandCaptor.getValue().reverseForNegativeStrand);
  }

  @Test
  public void run_SetAnnotationsSize_InvalidSize() throws Throwable {
    mainService.run(new String[] { SET_ANNOTATIONS_SIZE_COMMAND, "-s", "a" });
    verify(bedTransform, never()).setAnnotationsSize(any());
  }

  @Test
  public void run_SetAnnotationsSize_NegativeSize() throws Throwable {
    mainService.run(new String[] { SET_ANNOTATIONS_SIZE_COMMAND, "-s", "-2" });
    verify(bedTransform, never()).setAnnotationsSize(any());
  }

  @Test
  public void run_SetAnnotationsSize_ChangeStart() throws Throwable {
    mainService.run(new String[] { SET_ANNOTATIONS_SIZE_COMMAND, "-s", "1", "-c" });
    verify(bedTransform).setAnnotationsSize(setAnnotationsSizeCommandCaptor.capture());
    assertEquals((Integer) 1, setAnnotationsSizeCommandCaptor.getValue().size);
    assertEquals(true, setAnnotationsSizeCommandCaptor.getValue().changeStart);
    assertEquals(false, setAnnotationsSizeCommandCaptor.getValue().reverseForNegativeStrand);
  }

  @Test
  public void run_SetAnnotationsSize_ChangeStart_LongName() throws Throwable {
    mainService.run(new String[] { SET_ANNOTATIONS_SIZE_COMMAND, "-s", "1", "--changeStart" });
    verify(bedTransform).setAnnotationsSize(setAnnotationsSizeCommandCaptor.capture());
    assertEquals((Integer) 1, setAnnotationsSizeCommandCaptor.getValue().size);
    assertEquals(true, setAnnotationsSizeCommandCaptor.getValue().changeStart);
    assertEquals(false, setAnnotationsSizeCommandCaptor.getValue().reverseForNegativeStrand);
  }

  @Test
  public void run_SetAnnotationsSize_ReverseForNegativeStrand() throws Throwable {
    mainService.run(new String[] { SET_ANNOTATIONS_SIZE_COMMAND, "-s", "1", "-r" });
    verify(bedTransform).setAnnotationsSize(setAnnotationsSizeCommandCaptor.capture());
    assertEquals((Integer) 1, setAnnotationsSizeCommandCaptor.getValue().size);
    assertEquals(false, setAnnotationsSizeCommandCaptor.getValue().changeStart);
    assertEquals(true, setAnnotationsSizeCommandCaptor.getValue().reverseForNegativeStrand);
  }

  @Test
  public void run_SetAnnotationsSize_ReverseForNegativeStrand_LongName() throws Throwable {
    mainService.run(
        new String[] { SET_ANNOTATIONS_SIZE_COMMAND, "-s", "1", "--reverseForNegativeStrand" });
    verify(bedTransform).setAnnotationsSize(setAnnotationsSizeCommandCaptor.capture());
    assertEquals((Integer) 1, setAnnotationsSizeCommandCaptor.getValue().size);
    assertEquals(false, setAnnotationsSizeCommandCaptor.getValue().changeStart);
    assertEquals(true, setAnnotationsSizeCommandCaptor.getValue().reverseForNegativeStrand);
  }

  @Test
  public void run_SetAnnotationsSize_Input() throws Throwable {
    Path input = temporaryFolder.getRoot().toPath().resolve("input.txt");
    Files.createFile(input);
    mainService
        .run(new String[] { SET_ANNOTATIONS_SIZE_COMMAND, "-i", input.toString(), "-s", "1" });
    verify(bedTransform).setAnnotationsSize(setAnnotationsSizeCommandCaptor.capture());
    assertEquals(input, setAnnotationsSizeCommandCaptor.getValue().input);
    assertEquals((Integer) 1, setAnnotationsSizeCommandCaptor.getValue().size);
    assertEquals(false, setAnnotationsSizeCommandCaptor.getValue().changeStart);
    assertEquals(false, setAnnotationsSizeCommandCaptor.getValue().reverseForNegativeStrand);
  }

  @Test
  public void run_SetAnnotationsSize_InputLongName() throws Throwable {
    Path input = temporaryFolder.getRoot().toPath().resolve("input.txt");
    Files.createFile(input);
    mainService
        .run(new String[] { SET_ANNOTATIONS_SIZE_COMMAND, "--input", input.toString(), "-s", "1" });
    verify(bedTransform).setAnnotationsSize(setAnnotationsSizeCommandCaptor.capture());
    assertEquals(input, setAnnotationsSizeCommandCaptor.getValue().input);
    assertEquals((Integer) 1, setAnnotationsSizeCommandCaptor.getValue().size);
    assertEquals(false, setAnnotationsSizeCommandCaptor.getValue().changeStart);
    assertEquals(false, setAnnotationsSizeCommandCaptor.getValue().reverseForNegativeStrand);
  }

  @Test
  public void run_SetAnnotationsSize_InputNotExists() throws Throwable {
    Path input = temporaryFolder.getRoot().toPath().resolve("input.txt");
    mainService
        .run(new String[] { SET_ANNOTATIONS_SIZE_COMMAND, "-i", input.toString(), "-s", "1" });
    verify(bedTransform, never()).setAnnotationsSize(any());
  }

  @Test
  public void run_SetAnnotationsSize_Output() throws Throwable {
    Path output = temporaryFolder.getRoot().toPath().resolve("output.txt");
    Files.createFile(output);
    mainService
        .run(new String[] { SET_ANNOTATIONS_SIZE_COMMAND, "-o", output.toString(), "-s", "1" });
    verify(bedTransform).setAnnotationsSize(setAnnotationsSizeCommandCaptor.capture());
    assertEquals(output, setAnnotationsSizeCommandCaptor.getValue().output);
    assertEquals((Integer) 1, setAnnotationsSizeCommandCaptor.getValue().size);
    assertEquals(false, setAnnotationsSizeCommandCaptor.getValue().changeStart);
    assertEquals(false, setAnnotationsSizeCommandCaptor.getValue().reverseForNegativeStrand);
  }

  @Test
  public void run_SetAnnotationsSize_OutputLongName() throws Throwable {
    Path output = temporaryFolder.getRoot().toPath().resolve("output.txt");
    Files.createFile(output);
    mainService.run(
        new String[] { SET_ANNOTATIONS_SIZE_COMMAND, "--output", output.toString(), "-s", "1" });
    verify(bedTransform).setAnnotationsSize(setAnnotationsSizeCommandCaptor.capture());
    assertEquals(output, setAnnotationsSizeCommandCaptor.getValue().output);
    assertEquals((Integer) 1, setAnnotationsSizeCommandCaptor.getValue().size);
    assertEquals(false, setAnnotationsSizeCommandCaptor.getValue().changeStart);
    assertEquals(false, setAnnotationsSizeCommandCaptor.getValue().reverseForNegativeStrand);
  }

  @Test
  public void run_SetAnnotationsSize_OutputNotExists() throws Throwable {
    Path output = temporaryFolder.getRoot().toPath().resolve("output.txt");
    mainService
        .run(new String[] { SET_ANNOTATIONS_SIZE_COMMAND, "-o", output.toString(), "-s", "1" });
    verify(bedTransform).setAnnotationsSize(setAnnotationsSizeCommandCaptor.capture());
    assertEquals(output, setAnnotationsSizeCommandCaptor.getValue().output);
    assertEquals((Integer) 1, setAnnotationsSizeCommandCaptor.getValue().size);
    assertEquals(false, setAnnotationsSizeCommandCaptor.getValue().changeStart);
    assertEquals(false, setAnnotationsSizeCommandCaptor.getValue().reverseForNegativeStrand);
  }

  @Test
  public void run_SetAnnotationsSize_Help() throws Throwable {
    mainService.run(new String[] { SET_ANNOTATIONS_SIZE_COMMAND, "-h", "-s", "1" });
    verify(bedTransform, never()).setAnnotationsSize(any());
  }

  @Test
  public void run_MoveAnnotations() throws Throwable {
    mainService.run(new String[] { MOVE_ANNOTATIONS_COMMAND, "-d", "20" });
    verify(bedTransform).moveAnnotations(moveAnnotationCommandCaptor.capture());
    assertEquals((Integer) 20, moveAnnotationCommandCaptor.getValue().distance);
  }

  @Test
  public void run_MoveAnnotations_LongName() throws Throwable {
    mainService.run(new String[] { MOVE_ANNOTATIONS_COMMAND, "-distance", "20" });
    verify(bedTransform).moveAnnotations(moveAnnotationCommandCaptor.capture());
    assertEquals((Integer) 20, moveAnnotationCommandCaptor.getValue().distance);
  }

  @Test
  public void run_MoveAnnotations_UpperCase() throws Throwable {
    mainService.run(new String[] { MOVE_ANNOTATIONS_COMMAND.toUpperCase(), "-d", "20" });
    verify(bedTransform).moveAnnotations(moveAnnotationCommandCaptor.capture());
    assertEquals((Integer) 20, moveAnnotationCommandCaptor.getValue().distance);
  }

  @Test
  public void run_MoveAnnotations_InvalidSize() throws Throwable {
    mainService.run(new String[] { MOVE_ANNOTATIONS_COMMAND, "-d", "a" });
    verify(bedTransform, never()).moveAnnotations(any());
  }

  @Test
  public void run_MoveAnnotations_NegativeSize() throws Throwable {
    mainService.run(new String[] { MOVE_ANNOTATIONS_COMMAND, "-d", "-30" });
    verify(bedTransform).moveAnnotations(moveAnnotationCommandCaptor.capture());
    assertEquals((Integer) (-30), moveAnnotationCommandCaptor.getValue().distance);
    assertEquals(false, moveAnnotationCommandCaptor.getValue().reverseForNegativeStrand);
  }

  @Test
  public void run_MoveAnnotations_ReverseForNegativeStrand() throws Throwable {
    mainService.run(new String[] { MOVE_ANNOTATIONS_COMMAND, "-d", "20", "-r" });
    verify(bedTransform).moveAnnotations(moveAnnotationCommandCaptor.capture());
    assertEquals((Integer) 20, moveAnnotationCommandCaptor.getValue().distance);
    assertEquals(true, moveAnnotationCommandCaptor.getValue().reverseForNegativeStrand);
  }

  @Test
  public void run_MoveAnnotations_ReverseForNegativeStrand_LongName() throws Throwable {
    mainService
        .run(new String[] { MOVE_ANNOTATIONS_COMMAND, "-d", "20", "--reverseForNegativeStrand" });
    verify(bedTransform).moveAnnotations(moveAnnotationCommandCaptor.capture());
    assertEquals((Integer) 20, moveAnnotationCommandCaptor.getValue().distance);
    assertEquals(true, moveAnnotationCommandCaptor.getValue().reverseForNegativeStrand);
  }

  @Test
  public void run_MoveAnnotations_Input() throws Throwable {
    Path input = temporaryFolder.getRoot().toPath().resolve("input.txt");
    Files.createFile(input);
    mainService.run(new String[] { MOVE_ANNOTATIONS_COMMAND, "-i", input.toString(), "-d", "20" });
    verify(bedTransform).moveAnnotations(moveAnnotationCommandCaptor.capture());
    assertEquals(input, moveAnnotationCommandCaptor.getValue().input);
    assertEquals((Integer) 20, moveAnnotationCommandCaptor.getValue().distance);
    assertEquals(false, moveAnnotationCommandCaptor.getValue().reverseForNegativeStrand);
  }

  @Test
  public void run_MoveAnnotations_InputLongName() throws Throwable {
    Path input = temporaryFolder.getRoot().toPath().resolve("input.txt");
    Files.createFile(input);
    mainService
        .run(new String[] { MOVE_ANNOTATIONS_COMMAND, "--input", input.toString(), "-d", "20" });
    verify(bedTransform).moveAnnotations(moveAnnotationCommandCaptor.capture());
    assertEquals(input, moveAnnotationCommandCaptor.getValue().input);
    assertEquals((Integer) 20, moveAnnotationCommandCaptor.getValue().distance);
    assertEquals(false, moveAnnotationCommandCaptor.getValue().reverseForNegativeStrand);
  }

  @Test
  public void run_MoveAnnotations_InputNotExists() throws Throwable {
    Path input = temporaryFolder.getRoot().toPath().resolve("input.txt");
    mainService.run(new String[] { MOVE_ANNOTATIONS_COMMAND, "-i", input.toString(), "-d", "20" });
    verify(bedTransform, never()).moveAnnotations(any());
  }

  @Test
  public void run_MoveAnnotations_Output() throws Throwable {
    Path output = temporaryFolder.getRoot().toPath().resolve("output.txt");
    Files.createFile(output);
    mainService.run(new String[] { MOVE_ANNOTATIONS_COMMAND, "-o", output.toString(), "-d", "20" });
    verify(bedTransform).moveAnnotations(moveAnnotationCommandCaptor.capture());
    assertEquals(output, moveAnnotationCommandCaptor.getValue().output);
    assertEquals((Integer) 20, moveAnnotationCommandCaptor.getValue().distance);
    assertEquals(false, moveAnnotationCommandCaptor.getValue().reverseForNegativeStrand);
  }

  @Test
  public void run_MoveAnnotations_OutputLongName() throws Throwable {
    Path output = temporaryFolder.getRoot().toPath().resolve("output.txt");
    Files.createFile(output);
    mainService
        .run(new String[] { MOVE_ANNOTATIONS_COMMAND, "--output", output.toString(), "-d", "20" });
    verify(bedTransform).moveAnnotations(moveAnnotationCommandCaptor.capture());
    assertEquals(output, moveAnnotationCommandCaptor.getValue().output);
    assertEquals((Integer) 20, moveAnnotationCommandCaptor.getValue().distance);
    assertEquals(false, moveAnnotationCommandCaptor.getValue().reverseForNegativeStrand);
  }

  @Test
  public void run_MoveAnnotations_OutputNotExists() throws Throwable {
    Path output = temporaryFolder.getRoot().toPath().resolve("output.txt");
    mainService.run(new String[] { MOVE_ANNOTATIONS_COMMAND, "-o", output.toString(), "-d", "20" });
    verify(bedTransform).moveAnnotations(moveAnnotationCommandCaptor.capture());
    assertEquals(output, moveAnnotationCommandCaptor.getValue().output);
    assertEquals((Integer) 20, moveAnnotationCommandCaptor.getValue().distance);
    assertEquals(false, moveAnnotationCommandCaptor.getValue().reverseForNegativeStrand);
  }

  @Test
  public void run_MoveAnnotations_Help() throws Throwable {
    mainService.run(new String[] { MOVE_ANNOTATIONS_COMMAND, "-h", "-d", "1" });
    verify(bedTransform, never()).moveAnnotations(any());
  }

  @Test
  public void run_fastaToSizes() throws Throwable {
    mainService.run(new String[] { FASTA_TO_SIZES_COMMAND });
    verify(fastaConverter).toSizes(fastaToSizesCommandCaptor.capture());
  }

  @Test
  public void run_fastaToSizes_Input() throws Throwable {
    Path input = temporaryFolder.getRoot().toPath().resolve("input.txt");
    Files.createFile(input);
    mainService.run(new String[] { FASTA_TO_SIZES_COMMAND, "-i", input.toString() });
    verify(fastaConverter).toSizes(fastaToSizesCommandCaptor.capture());
    assertEquals(input, fastaToSizesCommandCaptor.getValue().input);
  }

  @Test
  public void run_fastaToSizes_InputLongName() throws Throwable {
    Path input = temporaryFolder.getRoot().toPath().resolve("input.txt");
    Files.createFile(input);
    mainService.run(new String[] { FASTA_TO_SIZES_COMMAND, "--input", input.toString() });
    verify(fastaConverter).toSizes(fastaToSizesCommandCaptor.capture());
    assertEquals(input, fastaToSizesCommandCaptor.getValue().input);
  }

  @Test
  public void run_fastaToSizes_InputNotExists() throws Throwable {
    Path input = temporaryFolder.getRoot().toPath().resolve("input.txt");
    mainService.run(new String[] { FASTA_TO_SIZES_COMMAND, "-i", input.toString() });
    verify(fastaConverter, never()).toSizes(any());
  }

  @Test
  public void run_fastaToSizes_Output() throws Throwable {
    Path output = temporaryFolder.getRoot().toPath().resolve("output.txt");
    Files.createFile(output);
    mainService.run(new String[] { FASTA_TO_SIZES_COMMAND, "-o", output.toString() });
    verify(fastaConverter).toSizes(fastaToSizesCommandCaptor.capture());
    assertEquals(output, fastaToSizesCommandCaptor.getValue().output);
  }

  @Test
  public void run_fastaToSizes_OutputLongName() throws Throwable {
    Path output = temporaryFolder.getRoot().toPath().resolve("output.txt");
    Files.createFile(output);
    mainService.run(new String[] { FASTA_TO_SIZES_COMMAND, "--output", output.toString() });
    verify(fastaConverter).toSizes(fastaToSizesCommandCaptor.capture());
    assertEquals(output, fastaToSizesCommandCaptor.getValue().output);
  }

  @Test
  public void run_fastaToSizes_OutputNotExists() throws Throwable {
    Path output = temporaryFolder.getRoot().toPath().resolve("output.txt");
    mainService.run(new String[] { FASTA_TO_SIZES_COMMAND, "-o", output.toString() });
    verify(fastaConverter).toSizes(fastaToSizesCommandCaptor.capture());
    assertEquals(output, fastaToSizesCommandCaptor.getValue().output);
  }

  @Test
  public void run_fastaToSizes_Help() throws Throwable {
    mainService.run(new String[] { FASTA_TO_SIZES_COMMAND, "-h" });
    verify(fastaConverter, never()).toSizes(any());
  }

  @Test
  public void run_Other() throws Throwable {
    mainService.run(new String[] { "other" });
    verifyZeroInteractions(bedTransform);
    verifyZeroInteractions(fastaConverter);
  }
}
