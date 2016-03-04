package exception;

import java.util.HashMap;

public class DAOExceptionMessage {
	
	private static HashMap<String, String> message = new HashMap<>();
	
	public static String getErrorMessage(String messageNumber){
		message.put("1", "Ajout impossible, l'exerice n'existe pas");
		message.put("2", "Problèle lors de l'ajout de l'exercice");
		
		message.put("3", "Suppression impossible, l'exerice n'existe pas");
		message.put("4", "Suppression impossible, l'exerice n'est pas présent dans la feuille d'exercices");
		
		message.put("5", "L'utilisateur n'a pas de feuille d'exercices");
		message.put("6", "La feuille d'exercices ne contient pas d'exercices");
		
		message.put("7", "L'exercice demandé n'existe pas");
		
		message.put("8", "L'utilisateur n'existe pas");
		
		message.put("9", "Problème lors de l'enregistrement");
		message.put("10", "La feuille d'exercices demandée n'existe pas");
		
		message.put("11", "Pas de feuille d'exercices créée");
		
		message.put("12", "Plusieurs exercices ont le même id");
		
		message.put("13", "Plusieurs utilisateurs ont le même id");
		
		message.put("14", "Plusieurs feuille d'exercices ont le même id");
		
		message.put("15", "Un utilisateur existe déjà pour cet email");
		
		message.put("20", "Une traduction en Français existe déjà pour ce mot");
		
		message.put("30", "Le mot-clés demandé n'existe pas");
		
		message.put("40", "L'utilisateur n'a pas le droit de supprimer cette feuille");

		message.put("50", "Problème lors de l'execution de la requête");
		
		message.put("51", "Problème lors de l'execution de la requête. Rollback impossible.");
		
		message.put("60", "La catégorie demandée n'existe pas");
		message.put("61", "La catégorie demandée n'a pas de sous catégorie");
		message.put("62", "Impossible d'efectuer votre demande");
				
		message.put("70", "Vérifier les paramètres envoyés");
		
		message.put("80", "L'utilisateur à déjà voté");
		
		message.put("90", "Aucune configuration n'a été trouvée");
		
		return message.get(messageNumber);
	}

}
