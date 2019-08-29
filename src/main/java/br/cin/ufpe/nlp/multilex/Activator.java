package br.cin.ufpe.nlp.multilex;

import java.io.IOException;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.cin.ufpe.nlp.api.tokenization.Tokenizer;
import br.cin.ufpe.nlp.api.tokenization.TokenizerFactory;
import br.cin.ufpe.nlp.util.AnnotatedToken;

public class Activator implements BundleActivator{
	private String inputPath;
	private String outputPath;
	private String lexLevels;
	private static Logger logger = LoggerFactory.getLogger(Activator.class);
	
	private class MyServiceListener implements ServiceListener {
		private boolean firstTime = true;
		private BundleContext context;
		
		public MyServiceListener(BundleContext context) {
			this.context = context;
		}

		@Override
		public void serviceChanged(ServiceEvent ev) {
			if (ev.getType() == ServiceEvent.REGISTERED || ev.getType() == ServiceEvent.MODIFIED) {
				if (firstTime) {
					firstTime = false;
					ServiceReference servRef = ev.getServiceReference();
					@SuppressWarnings("unchecked")
					TokenizerFactory<Tokenizer<AnnotatedToken>> tokenizerFactory = (TokenizerFactory<Tokenizer<AnnotatedToken>>) context.getService(servRef);
					try {
						doStuff(tokenizerFactory);
					} catch (IOException e) {
						e.printStackTrace();
						throw new IllegalStateException(e);
					}
					logger.info("ALL DONE, now trying to shutdown gracefully");
					try {
						context.getBundle(0).stop();
					} catch (BundleException e) {
						e.printStackTrace();
						throw new IllegalStateException("Error while trying to shutdown OSGI after all done", e);
					}
				}
			}
			
		}
	}
	
	private void doStuff(TokenizerFactory<Tokenizer<AnnotatedToken>> tokenizerFactory) throws IOException {
		MultilexController mc = new MultilexController(inputPath, outputPath, lexLevels, tokenizerFactory);
		mc.process();
	}
	

	public void start(BundleContext context) throws Exception {
		inputPath = System.getenv("MULTILEX_INPUTPATH"); //context.getProperty("multilex.inputpath");
		if (inputPath == null) {
			throw new IllegalArgumentException("Missing -Dmultilex.inputpath=<input_path>");
		}
		outputPath = System.getenv("MULTILEX_OUTPUTPATH"); //context.getProperty("multilex.outputpath");
		if (outputPath == null) {
			throw new IllegalArgumentException("Missing -Dmultilex.outputpath=<output_path>");
		}
		lexLevels = System.getenv("MULTILEX_LEXLEVELS"); //context.getProperty("multilex.lexlevels");
		if (lexLevels == null) {
			throw new IllegalArgumentException("Missing -Dmultilex.lexlevels=words,lemmas,lemmapos,ner,ssenses (omit undesired ones)");
		}
		
		
		ServiceReference[] services = context.getServiceReferences(TokenizerFactory.class.getName(), "(type=multiannot)");
		if (services == null) {
			System.err.println("WARNING: TokenizerFactory service not found, waiting for it");
			context.addServiceListener(new MyServiceListener(context), "(&(" + Constants.OBJECTCLASS + "=" + TokenizerFactory.class.getName() + ")(type=multiannot))");
		} else {
			@SuppressWarnings("unchecked")
			TokenizerFactory<Tokenizer<AnnotatedToken>> tokenizerFactory = (TokenizerFactory<Tokenizer<AnnotatedToken>>) context.getService(services[0]);
			doStuff(tokenizerFactory);
			logger.info("ALL DONE, now trying to shutdown gracefully");
			try {
				context.getBundle(0).stop();
			} catch (BundleException e) {
				e.printStackTrace();
				throw new IllegalStateException("Error while trying to shutdown OSGI after all done", e);
			}
		}
		
	}

	public void stop(BundleContext context) throws Exception {
	
	}


}
