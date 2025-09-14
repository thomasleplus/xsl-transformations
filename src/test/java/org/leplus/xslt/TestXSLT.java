package org.leplus.xslt;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.lib.FeatureKeys;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLUnit;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xml.sax.SAXException;

/** Tests XSL transformations. */
public class TestXSLT {

  /** The main resources directory. */
  private static final String MAIN_RESOURCES_DIR = "src/main/resources";

  /** The test resources directory. */
  private static final String TEST_RESOURCES_DIR = "src/test/resources";

  /** The build directory. */
  private static final String BUILD_DIR = "target/resources";

  /** The XSLT file extensions. */
  private static final String[] XSLT_EXTENSIONS = {"xsl", "xslt"};

  /** The input file prefix. */
  private static final String INPUT_FILE_PREFIX = "input";

  /** The output file prefix. */
  private static final String OUTPUT_FILE_PREFIX = "output";

  /** The XML content type. */
  private static final String XML = "xml";

  /** The JSON content type. */
  private static final String JSON = "json";

  /**
   * Tests all XSL transformations.
   *
   * @throws IOException if an I/O error occurs
   * @throws TransformerException if an unrecoverable error occurs during the course of the
   *     transformation
   * @throws SAXException if a SAX error occurs
   */
  @Test
  public void test() throws IOException, TransformerException, SAXException {
    XMLUnit.setIgnoreWhitespace(true);
    final Collection<File> xsltFiles =
        FileUtils.listFiles(new File(MAIN_RESOURCES_DIR), XSLT_EXTENSIONS, true);
    for (final File xsltFile : xsltFiles) {
      testXSLTs(xsltFile);
    }
  }

  private void testXSLTs(final File xsltFile)
      throws IOException, TransformerException, SAXException {
    final String testDirName = xsltFile.getPath().replace(MAIN_RESOURCES_DIR, TEST_RESOURCES_DIR);
    final File testDir = new File(testDirName.substring(0, testDirName.lastIndexOf('.')));
    if (!testDir.exists()) {
      return;
    }
    final Collection<File> inputFiles =
        FileUtils.listFiles(
            testDir, new PrefixFileFilter(INPUT_FILE_PREFIX), FileFilterUtils.trueFileFilter());
    for (final File inputFile : inputFiles) {
      final Collection<File> candidateOutputFiles =
          FileUtils.listFiles(
              inputFile.getParentFile(),
              new PrefixFileFilter(
                  inputFile
                      .getName()
                      .substring(0, inputFile.getName().lastIndexOf('.'))
                      .replace(INPUT_FILE_PREFIX, OUTPUT_FILE_PREFIX)),
              FileFilterUtils.trueFileFilter());
      if (candidateOutputFiles.isEmpty()) {
        fail("Missing expected output file for input file " + inputFile);
      } else if (candidateOutputFiles.size() > 1) {
        fail(
            "Multiple expected output files for input file "
                + inputFile
                + ": "
                + candidateOutputFiles);
      } else {
        testXSLT(xsltFile, inputFile, candidateOutputFiles.iterator().next());
      }
    }
  }

  private void testXSLT(final File xsltFile, final File inputFile, final File expectedOutputFile)
      throws TransformerException, SAXException, IOException {
    final File actualOutputFile = applyXSLT(inputFile, xsltFile);
    final String type = Files.probeContentType(actualOutputFile.toPath());
    if (type != null && (type.startsWith(XML) || type.endsWith(XML))) {
      compareXML(expectedOutputFile, actualOutputFile);
    } else if (type != null && (type.startsWith(JSON) || type.endsWith(JSON))) {
      compareJSON(expectedOutputFile, actualOutputFile);
    } else {
      compareFile(expectedOutputFile, actualOutputFile);
    }
  }

  private void compareJSON(final File expectedOutputFile, final File actualOutputFile) {
    assertThat(actualOutputFile, isSameJSONas(expectedOutputFile));
  }

  private Matcher<File> isSameJSONas(final File expected) {
    return new JSONMatcher(expected);
  }

  private static final class JSONMatcher extends BaseMatcher<File> {

    /** The expected file. */
    private final File expected;

    private JSONMatcher(final File expectedFile) {
      this.expected = expectedFile;
    }

    @Override
    public boolean matches(final Object item) {
      try {
        return !JSONCompare.compareJSON(
                FileUtils.readFileToString(expected, StandardCharsets.UTF_8),
                FileUtils.readFileToString((File) item, StandardCharsets.UTF_8),
                JSONCompareMode.STRICT)
            .failed();
      } catch (JSONException | IOException e) {
        throw new AssertionError(e);
      }
    }

    @Override
    public void describeTo(final Description description) {
      description.appendText("has same JSON content as " + expected);
    }
  }

  private void compareFile(final File expectedOutputFile, final File actualOutputFile)
      throws IOException {
    assertTrue(
        FileUtils.contentEquals(expectedOutputFile, actualOutputFile),
        "Files differ (expected: " + expectedOutputFile + ", actual: " + actualOutputFile + ")");
  }

  private void compareXML(final File expectedOutputFile, final File actualOutputFile)
      throws SAXException, IOException {
    try (Reader actualOutputFileReader = new FileReader(actualOutputFile);
        Reader expectedOutputFileReader = new FileReader(expectedOutputFile)) {
      final DetailedDiff detailXmlDiff =
          new DetailedDiff(new Diff(actualOutputFileReader, expectedOutputFileReader));
      @SuppressWarnings("unchecked")
      final List<Difference> differences = detailXmlDiff.getAllDifferences();
      for (final Difference difference : differences) {
        fail(difference.toString());
      }
    }
  }

  private File applyXSLT(final File inputFile, final File xsltFile)
      throws TransformerException, IOException {
    final File outputFile = new File(inputFile.getPath().replace(TEST_RESOURCES_DIR, BUILD_DIR));
    outputFile.getParentFile().mkdirs();
    try (FileInputStream xsltStream = new FileInputStream(xsltFile);
        FileInputStream inputStream = new FileInputStream(inputFile);
        FileOutputStream outputStream = new FileOutputStream(outputFile)) {
      final TransformerFactory tf = TransformerFactory.newInstance();
      tf.setAttribute(FeatureKeys.ALLOW_EXTERNAL_FUNCTIONS, false); // Security
      tf.newTransformer(new StreamSource(xsltStream))
          .transform(new StreamSource(inputStream), new StreamResult(outputStream));
    }
    return outputFile;
  }
}
