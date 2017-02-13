package br.cin.ufpe.nlp.multilex;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import br.cin.ufpe.nlp.api.tokenization.Tokenizer;
import br.cin.ufpe.nlp.api.tokenization.TokenizerFactory;
import br.cin.ufpe.nlp.api.transform.DocumentProcessorOneToN;
import br.cin.ufpe.nlp.util.AnnotatedToken;
import br.cin.ufpe.nlp.util.TokenAnnotation;

public class MultilexDocumentProcessor implements DocumentProcessorOneToN {
	
	private TokenizerFactory<Tokenizer<AnnotatedToken>> tokenizerFactory;
	
	private String[] lexLevels;

	public MultilexDocumentProcessor(TokenizerFactory<Tokenizer<AnnotatedToken>> tokenizerFactory, String[] lexLevels) {
		this.tokenizerFactory = tokenizerFactory;
		this.lexLevels = lexLevels;
	}

	@Override
	public void processDocument(Reader inputDocument, Writer[] outputDocuments) throws IOException {
		assert(outputDocuments.length == lexLevels.length);
		Tokenizer<AnnotatedToken> tokenizer = tokenizerFactory.create(inputDocument);
		while(tokenizer.hasMoreTokens()) {
			AnnotatedToken token = tokenizer.nextToken();
			for (int i = 0; i < lexLevels.length; i++) {
				// words,lemmas,lemmapos,ner,ssenses
				if (lexLevels[i].equals("words")) {
					outputDocuments[i].append(token.getTokenText());
					outputDocuments[i].append('\n');
				} else if (lexLevels[i].equals("lemmas")) {
					outputDocuments[i].append(token.getAnnotations().get(TokenAnnotation.LEMMA));
					outputDocuments[i].append('\n');
				} else if (lexLevels[i].equals("lemmapos")) {
					outputDocuments[i].append(token.getAnnotations().get(TokenAnnotation.LEMMA));
					outputDocuments[i].append('_');
					outputDocuments[i].append(token.getAnnotations().get(TokenAnnotation.POSTAG));					
					outputDocuments[i].append('\n');
				} else if (lexLevels[i].equals("ner")) {
					outputDocuments[i].append(token.getAnnotations().get(TokenAnnotation.NER));
					outputDocuments[i].append('\n');
				} else if (lexLevels[i].equals("ssenses")) {
					String ssenseString = token.getAnnotations().get(TokenAnnotation.SUPERSENSE);
					int hifenIndex = ssenseString.indexOf('-');
					if (hifenIndex >= 0) {
						ssenseString = ssenseString.substring(hifenIndex+1);
					}
					outputDocuments[i].append(ssenseString);
					outputDocuments[i].append('\n');
				} else {
					throw new IllegalArgumentException("Lexicalization level '" + lexLevels[i] + "' is unknown");
				}
			}
		}
	}

}
