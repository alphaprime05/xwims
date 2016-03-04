package exception;

import java.util.HashMap;

public class ServiceExceptionMessage {
	
	private static HashMap<String, String> message = new HashMap<>();
	
	public static String getErrorMessage(String messageNumber){
		message.put("101", "Argument manquant.");
		message.put("102", "Argument incomplet.");
		message.put("103", "Problème avec le format d'un des arguments.");
		
		message.put("111", "Droits insuffistants pour effectuer l'action demandée.");

		message.put("121", "Une erreur s'est produite lors de l'authentification.");

		message.put("131", "Aucune feuille d'exercice n'existe pour l'utilisateur courant.");
		
		message.put("141", "Erreur lors de l'enregistrement de la feuille.");
		
		message.put("151", "L'utilisateur à déjà voté.");
		
		message.put("161", "Erreur lors du chiffrement du mot de passe.");
		message.put("162", "Erreur lors de l'envoie de l'email d'inscription.");

		return message.get(messageNumber);
	}
}
