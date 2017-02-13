package br.cin.ufpe.nlp.multilex;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import br.cin.ufpe.nlp.api.tokenization.Tokenizer;
import br.cin.ufpe.nlp.api.tokenization.TokenizerFactory;
import br.cin.ufpe.nlp.api.transform.DocumentProcessorOneToN;
import br.cin.ufpe.nlp.util.AnnotatedToken;
import br.cin.ufpe.nlp.util.RecursiveTransformer;

public class MultilexController {
	private String inputPath;
	private String outputPath;
	private String lexLevels;
	private TokenizerFactory<Tokenizer<AnnotatedToken>> tokenizerFactory;

	public MultilexController(String inputPath, String outputPath, String lexLevels, TokenizerFactory<Tokenizer<AnnotatedToken>> tokenizerFactory) {
		this.inputPath = inputPath;
		this.outputPath = outputPath;
		this.lexLevels = lexLevels;
		this.tokenizerFactory = tokenizerFactory;
	}
	
	public void process() throws IOException {
		StringTokenizer lexToken = new StringTokenizer(this.lexLevels, ",");
		File outDirs[] = new File[lexToken.countTokens()];
		int i = 0;
		String[] lexLevels = new String[outDirs.length];
		while (lexToken.hasMoreTokens()) {
			lexLevels[i] = lexToken.nextToken();
			File newOutDir = new File(new File(outputPath), lexLevels[i]);
			newOutDir.mkdir();
			outDirs[i++] = newOutDir;
		}
		DocumentProcessorOneToN docProcessor = new MultilexDocumentProcessor(tokenizerFactory, lexLevels);
		RecursiveTransformer.recursiveProcess(new File(inputPath), outDirs, docProcessor, 1.0);
	}

}
