/**
 * Classe qui représenter la table des symboles
 */
package fr.ul.miage.projet;
import java.util.HashMap;

public class TableDesSymboles {

	/**
	 * A chaque variable, on associe ses caractéristiques (stockées dans une hashmap)
	 */
	private HashMap<Variable, HashMap<String, String>> tds;

	/**
	 * Constructeur d'une tds vide
	 */
	public TableDesSymboles() {
		super();
		this.tds = new HashMap<Variable, HashMap<String,String>>();
	}
	
	/**
	 * Constructeur d'une tds à partir d'une tds
	 * @param tds
	 */
	public TableDesSymboles(HashMap<Variable, HashMap<String, String>> tds) {
		super();
		this.tds = tds;
	}

	/**
	 * Méthode de recherche
	 */
	public HashMap<String, String> rechercher(String idf, int scope) {
		//TODO
		return null;
	}
	
	/**
	 * Méthode d'insertion d'une variables globales
	 */
	public void insertion(String idf, int scope, String type, int val) {
		//TODO
	}
	
	/**
	 * Méthode d'insertion ...
	 */
	//TODO
	
	/**
	 * @return the tds
	 */
	public HashMap<Variable, HashMap<String, String>> getTds() {
		return tds;
	}

	/**
	 * @param tds the tds to set
	 */
	public void setTds(HashMap<Variable, HashMap<String, String>> tds) {
		this.tds = tds;
	}
	
	
}
