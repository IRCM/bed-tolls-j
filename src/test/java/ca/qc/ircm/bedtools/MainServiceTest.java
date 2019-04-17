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

import static ca.qc.ircm.bedtools.BedpeToBedCommand.BEDPE_TO_BED;
import static ca.qc.ircm.bedtools.FastaToSizesCommand.FASTA_TO_SIZES_COMMAND;
import static ca.qc.ircm.bedtools.FilterBedpeCommand.FILTER_BEDPE;
import static ca.qc.ircm.bedtools.MoveAnnotationsCommand.MOVE_ANNOTATIONS_COMMAND;
import static ca.qc.ircm.bedtools.SetAnnotationsSizeCommand.SET_ANNOTATIONS_SIZE_COMMAND;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import ca.qc.ircm.bedtools.test.config.NonTransactionalTestAnnotations;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class MainServiceTest {
  @Inject
  private MainService mainService;
  @MockBean
  private BedTransform bedTransform;
  @MockBean
  private FastaConverter fastaConverter;
  @MockBean
  private FilterBedpe filterBedpe;
  @MockBean
  private BedpeToBed bedpeToBed;
  @Captor
  private ArgumentCaptor<SetAnnotationsSizeCommand> setAnnotationsSizeCommandCaptor;
  @Captor
  private ArgumentCaptor<MoveAnnotationsCommand> moveAnnotationCommandCaptor;
  @Captor
  private ArgumentCaptor<FastaToSizesCommand> fastaToSizesCommandCaptor;
  @Captor
  private ArgumentCaptor<FilterBedpeCommand> filterBedpeCommandCaptor;
  @Captor
  private ArgumentCaptor<BedpeToBedCommand> bedpeToBedCommandCaptor;
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Before
  public void beforeTest() {
    mainService.setRunnerEnabled(true);
  }

  @Test
  public void run_RunnerDisabled() {
    mainService.setRunnerEnabled(false);
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
    assertEquals(false, moveAnnotationCommandCaptor.getValue().discardNegative);
  }

  @Test
  public void run_MoveAnnotations_ReverseForNegativeStrand() throws Throwable {
    mainService.run(new String[] { MOVE_ANNOTATIONS_COMMAND, "-d", "20", "-r" });
    verify(bedTransform).moveAnnotations(moveAnnotationCommandCaptor.capture());
    assertEquals((Integer) 20, moveAnnotationCommandCaptor.getValue().distance);
    assertEquals(true, moveAnnotationCommandCaptor.getValue().reverseForNegativeStrand);
    assertEquals(false, moveAnnotationCommandCaptor.getValue().discardNegative);
  }

  @Test
  public void run_MoveAnnotations_ReverseForNegativeStrand_LongName() throws Throwable {
    mainService
        .run(new String[] { MOVE_ANNOTATIONS_COMMAND, "-d", "20", "--reverseForNegativeStrand" });
    verify(bedTransform).moveAnnotations(moveAnnotationCommandCaptor.capture());
    assertEquals((Integer) 20, moveAnnotationCommandCaptor.getValue().distance);
    assertEquals(true, moveAnnotationCommandCaptor.getValue().reverseForNegativeStrand);
    assertEquals(false, moveAnnotationCommandCaptor.getValue().discardNegative);
  }

  @Test
  public void run_MoveAnnotations_DiscardNegatives() throws Throwable {
    mainService.run(new String[] { MOVE_ANNOTATIONS_COMMAND, "-d", "20", "-dn" });
    verify(bedTransform).moveAnnotations(moveAnnotationCommandCaptor.capture());
    assertEquals((Integer) 20, moveAnnotationCommandCaptor.getValue().distance);
    assertEquals(false, moveAnnotationCommandCaptor.getValue().reverseForNegativeStrand);
    assertEquals(true, moveAnnotationCommandCaptor.getValue().discardNegative);
  }

  @Test
  public void run_MoveAnnotations_DiscardNegatives_LongName() throws Throwable {
    mainService.run(new String[] { MOVE_ANNOTATIONS_COMMAND, "-d", "20", "--discardNegatives" });
    verify(bedTransform).moveAnnotations(moveAnnotationCommandCaptor.capture());
    assertEquals((Integer) 20, moveAnnotationCommandCaptor.getValue().distance);
    assertEquals(false, moveAnnotationCommandCaptor.getValue().reverseForNegativeStrand);
    assertEquals(true, moveAnnotationCommandCaptor.getValue().discardNegative);
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
    assertEquals(false, moveAnnotationCommandCaptor.getValue().discardNegative);
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
    assertEquals(false, moveAnnotationCommandCaptor.getValue().discardNegative);
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
    assertEquals(false, moveAnnotationCommandCaptor.getValue().discardNegative);
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
    assertEquals(false, moveAnnotationCommandCaptor.getValue().discardNegative);
  }

  @Test
  public void run_MoveAnnotations_OutputNotExists() throws Throwable {
    Path output = temporaryFolder.getRoot().toPath().resolve("output.txt");
    mainService.run(new String[] { MOVE_ANNOTATIONS_COMMAND, "-o", output.toString(), "-d", "20" });
    verify(bedTransform).moveAnnotations(moveAnnotationCommandCaptor.capture());
    assertEquals(output, moveAnnotationCommandCaptor.getValue().output);
    assertEquals((Integer) 20, moveAnnotationCommandCaptor.getValue().distance);
    assertEquals(false, moveAnnotationCommandCaptor.getValue().reverseForNegativeStrand);
    assertEquals(false, moveAnnotationCommandCaptor.getValue().discardNegative);
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
  public void run_filterBedpe() throws Throwable {
    mainService.run(new String[] { FILTER_BEDPE });
    verify(filterBedpe).run(filterBedpeCommandCaptor.capture());
    assertNull(filterBedpeCommandCaptor.getValue().maximumInsertSize);
  }

  @Test
  public void run_filterBedpe_MaximumInsertSize() throws Throwable {
    mainService.run(new String[] { FILTER_BEDPE, "--maximumInsertSize", "200" });
    verify(filterBedpe).run(filterBedpeCommandCaptor.capture());
    assertEquals((Integer) 200, filterBedpeCommandCaptor.getValue().maximumInsertSize);
  }

  @Test
  public void run_filterBedpe_Input() throws Throwable {
    Path input = temporaryFolder.getRoot().toPath().resolve("input.txt");
    Files.createFile(input);
    mainService.run(new String[] { FILTER_BEDPE, "-i", input.toString() });
    verify(filterBedpe).run(filterBedpeCommandCaptor.capture());
    assertNull(filterBedpeCommandCaptor.getValue().maximumInsertSize);
    assertEquals(input, filterBedpeCommandCaptor.getValue().input);
  }

  @Test
  public void run_filterBedpe_InputLongName() throws Throwable {
    Path input = temporaryFolder.getRoot().toPath().resolve("input.txt");
    Files.createFile(input);
    mainService.run(new String[] { FILTER_BEDPE, "--input", input.toString() });
    verify(filterBedpe).run(filterBedpeCommandCaptor.capture());
    assertNull(filterBedpeCommandCaptor.getValue().maximumInsertSize);
    assertEquals(input, filterBedpeCommandCaptor.getValue().input);
  }

  @Test
  public void run_filterBedpe_InputNotExists() throws Throwable {
    Path input = temporaryFolder.getRoot().toPath().resolve("input.txt");
    mainService.run(new String[] { FILTER_BEDPE, "-i", input.toString() });
    verify(filterBedpe, never()).run(any());
  }

  @Test
  public void run_filterBedpe_Output() throws Throwable {
    Path output = temporaryFolder.getRoot().toPath().resolve("output.txt");
    Files.createFile(output);
    mainService.run(new String[] { FILTER_BEDPE, "-o", output.toString() });
    verify(filterBedpe).run(filterBedpeCommandCaptor.capture());
    assertNull(filterBedpeCommandCaptor.getValue().maximumInsertSize);
    assertEquals(output, filterBedpeCommandCaptor.getValue().output);
  }

  @Test
  public void run_filterBedpe_OutputLongName() throws Throwable {
    Path output = temporaryFolder.getRoot().toPath().resolve("output.txt");
    Files.createFile(output);
    mainService.run(new String[] { FILTER_BEDPE, "--output", output.toString() });
    verify(filterBedpe).run(filterBedpeCommandCaptor.capture());
    assertNull(filterBedpeCommandCaptor.getValue().maximumInsertSize);
    assertEquals(output, filterBedpeCommandCaptor.getValue().output);
  }

  @Test
  public void run_filterBedpe_OutputNotExists() throws Throwable {
    Path output = temporaryFolder.getRoot().toPath().resolve("output.txt");
    mainService.run(new String[] { FILTER_BEDPE, "-o", output.toString() });
    verify(filterBedpe).run(filterBedpeCommandCaptor.capture());
    assertNull(filterBedpeCommandCaptor.getValue().maximumInsertSize);
    assertEquals(output, filterBedpeCommandCaptor.getValue().output);
  }

  @Test
  public void run_filterBedpe_Help() throws Throwable {
    mainService.run(new String[] { FILTER_BEDPE, "-h" });
    verify(filterBedpe, never()).run(any());
  }

  @Test
  public void run_BedpeToBed() throws Throwable {
    mainService.run(new String[] { BEDPE_TO_BED });
    verify(bedpeToBed).run(any());
  }

  @Test
  public void run_BedpeToBed_Input() throws Throwable {
    Path input = temporaryFolder.getRoot().toPath().resolve("input.txt");
    Files.createFile(input);
    mainService.run(new String[] { BEDPE_TO_BED, "-i", input.toString() });
    verify(bedpeToBed).run(bedpeToBedCommandCaptor.capture());
    assertEquals(input, bedpeToBedCommandCaptor.getValue().input);
  }

  @Test
  public void run_BedpeToBed_InputLongName() throws Throwable {
    Path input = temporaryFolder.getRoot().toPath().resolve("input.txt");
    Files.createFile(input);
    mainService.run(new String[] { BEDPE_TO_BED, "--input", input.toString() });
    verify(bedpeToBed).run(bedpeToBedCommandCaptor.capture());
    assertEquals(input, bedpeToBedCommandCaptor.getValue().input);
  }

  @Test
  public void run_BedpeToBed_InputNotExists() throws Throwable {
    Path input = temporaryFolder.getRoot().toPath().resolve("input.txt");
    mainService.run(new String[] { BEDPE_TO_BED, "-i", input.toString() });
    verify(bedpeToBed, never()).run(any());
  }

  @Test
  public void run_BedpeToBed_Output() throws Throwable {
    Path output = temporaryFolder.getRoot().toPath().resolve("output.txt");
    Files.createFile(output);
    mainService.run(new String[] { BEDPE_TO_BED, "-o", output.toString() });
    verify(bedpeToBed).run(bedpeToBedCommandCaptor.capture());
    assertEquals(output, bedpeToBedCommandCaptor.getValue().output);
  }

  @Test
  public void run_BedpeToBed_OutputLongName() throws Throwable {
    Path output = temporaryFolder.getRoot().toPath().resolve("output.txt");
    Files.createFile(output);
    mainService.run(new String[] { BEDPE_TO_BED, "--output", output.toString() });
    verify(bedpeToBed).run(bedpeToBedCommandCaptor.capture());
    assertEquals(output, bedpeToBedCommandCaptor.getValue().output);
  }

  @Test
  public void run_BedpeToBed_OutputNotExists() throws Throwable {
    Path output = temporaryFolder.getRoot().toPath().resolve("output.txt");
    mainService.run(new String[] { BEDPE_TO_BED, "-o", output.toString() });
    verify(bedpeToBed).run(bedpeToBedCommandCaptor.capture());
    assertEquals(output, bedpeToBedCommandCaptor.getValue().output);
  }

  @Test
  public void run_BedpeToBed_Help() throws Throwable {
    mainService.run(new String[] { BEDPE_TO_BED, "-h" });
    verify(bedpeToBed, never()).run(any());
  }

  @Test
  public void run_Other() throws Throwable {
    mainService.run(new String[] { "other" });
    verifyZeroInteractions(bedTransform);
    verifyZeroInteractions(fastaConverter);
    verifyZeroInteractions(filterBedpe);
    verifyZeroInteractions(bedpeToBed);
  }
}
