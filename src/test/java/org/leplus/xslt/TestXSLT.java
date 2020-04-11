package org.leplus.xslt;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.xml.sax.SAXException;

public class TestXSLT {

	private static final String MAIN_RESOURCES_DIR = "src/main/resources";
	private static final String TEST_RESOURCES_DIR = "src/test/resources";
	private static final String BUILD_DIR = "target/resources";
	private static final String[] XSLT_EXTENSIONS = { "xsl", "xslt" };
	private static final String INPUT_FILE_PREFIX = "input";
	private static final String OUTPUT_FILE_PREFIX = "output";

	@Rule
	public ErrorCollector collector = new ErrorCollector();

	@Test
	public void test() throws IOException, TransformerException, SAXException {
		XMLUnit.setIgnoreWhitespace(true);
		final Collection<File> xsltFiles = FileUtils.listFiles(new File(MAIN_RESOURCES_DIR), XSLT_EXTENSIONS, true);
		for (final File xsltFile : xsltFiles) {
			testXSLTs(xsltFile);
		}
	}

	private void testXSLTs(final File xsltFile) throws IOException, TransformerException, SAXException {
		final String testDirName = xsltFile.getPath().replace(MAIN_RESOURCES_DIR, TEST_RESOURCES_DIR);
		final File testDir = new File(testDirName.substring(0, testDirName.lastIndexOf('.')));
		if (!testDir.exists()) {
			return;
		}
		final Collection<File> inputFiles = FileUtils.listFiles(testDir, new PrefixFileFilter(INPUT_FILE_PREFIX),
				FileFilterUtils.trueFileFilter());
		for (final File inputFile : inputFiles) {
			final Collection<File> candidateOutputFiles = FileUtils.listFiles(inputFile.getParentFile(),
					new PrefixFileFilter(inputFile.getName().substring(0, inputFile.getName().lastIndexOf('.'))
							.replace(INPUT_FILE_PREFIX, OUTPUT_FILE_PREFIX)),
					FileFilterUtils.trueFileFilter());
			if (candidateOutputFiles.size() == 0) {
				collector.addError(new Throwable("Missing expected output file for input file " + inputFile));
			} else if (candidateOutputFiles.size() > 1) {
				collector.addError(new Throwable(
						"Multiple expected output files for input file " + inputFile + ": " + candidateOutputFiles));
			} else {
				testXSLT(xsltFile, inputFile, candidateOutputFiles.iterator().next());
			}
		}
	}

	private void testXSLT(final File xsltFile, final File inputFile, final File expectedOutputFile)
			throws TransformerException, SAXException, IOException {
		final File actualOutputFile = applyXSLT(inputFile, xsltFile);
		final Reader actualOutputFileReader = new FileReader(actualOutputFile);
		final Reader expectedOutputFileReader = new FileReader(expectedOutputFile);
		final DetailedDiff detailXmlDiff = new DetailedDiff(new Diff(actualOutputFileReader, expectedOutputFileReader));
		@SuppressWarnings("unchecked")
		final List<Difference> differences = detailXmlDiff.getAllDifferences();
		for (Difference difference : differences) {
			collector.addError(new Throwable(difference.toString()));
		}
	}

	private File applyXSLT(final File inputFile, final File xsltFile) throws TransformerException {
		final TransformerFactory transfomerFactory = TransformerFactory.newInstance();
		final Transformer transformer = transfomerFactory.newTransformer(new StreamSource(xsltFile));
		final File outputFile = new File(inputFile.getPath().replace(TEST_RESOURCES_DIR, BUILD_DIR));
		transformer.transform(new StreamSource(inputFile), new StreamResult(outputFile));
		return outputFile;
	}

}
