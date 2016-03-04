package com.esipe.dbimport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class Main 
{
	public static void main( String[] args )
	{
		/*
		 * WARNING: TO IMPORT DATA FROM WIMS, THE CHMOD OF THE FOLDER wims/public_html/bases/sys MUST BE SET TO 644.
		 */
		
		if(args.length != 5) {
			System.out.println("USAGE: java -jar dbimport.jar [encoding_file] [wims_folder] [wims_url] [server_url] [wims_password]");
			return;
		}
		String wimsFolder = args[1];
		String wimsUrl = args[2];
		String serverUrl= args[3];
		String wimsPassword= args[4];
		Path langFiles = Paths.get(wimsFolder+"/public_html/bases/sys/");
		Path exercisesFiles = Paths.get(wimsFolder+"/public_html/modules/");	
		Path levelFile = Paths.get(wimsFolder+"/public_html/modules/adm/class/gateway/lang/names.phtml.fr");		
		Path confPath = Paths.get(args[0]);
		
		CharsetHandler handler = new CharsetHandler();
		try {
			Files.lines(confPath).forEach(line -> {
				String[] tokens = line.split(":");
				handler.putCharset(tokens[0], tokens[1]);
			});
		} catch (IOException e1) {
			System.err.println("ERROR: Unable to read configuration file. Aborting...");
			return;
		}

		if(!Files.exists(langFiles) || !Files.exists(exercisesFiles) || !Files.isDirectory(exercisesFiles) || !Files.isDirectory(langFiles)) {
			System.err.println("ERROR: "+ wimsFolder + "doesn't seem to be a correct wims folder. Aborting...");
			return;
		}
		
		
		try(DatabaseCollector collector = new DatabaseCollector(handler, langFiles, exercisesFiles, levelFile, wimsUrl, serverUrl, wimsPassword)) {
			collector.extractDatabase();
			//collector.indexFullDatabase();
		} catch (IOException e) {
			System.out.println("IOException... Aborting.");
		} catch (NoSuchAlgorithmException|InvalidKeySpecException e) {
			System.out.println("Enable to generate password... Aborting.");
		}
	}
}
