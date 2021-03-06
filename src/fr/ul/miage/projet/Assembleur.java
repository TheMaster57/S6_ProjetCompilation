/*
 * Classe qui génère l'assembleur en fonction de l'arbre et de la table des symboles
 */
package fr.ul.miage.projet;

import java.util.*;

/**
 *
 * @author Maxime BLAISE
 * @
 */
public class Assembleur {

    /**
     * Arbre.
     */
    public final Arbre arbre;

    /**
     * Table des symboles.
     */
    public final TableDesSymboles tds;

    /**
     * Résultat en assembleur.
     */
    public String res;

    /**
     * Map des opérateurs
     */
    public HashMap<String, String> mapOp = new HashMap<>();

    private String fonctionCourante = "";

    /**
     * Constructeur qui initialise les variables
     *
     * @param arbre Arbre
     * @param tds Table des symboles
     */
    public Assembleur(Arbre arbre, TableDesSymboles tds) {
        this.arbre = arbre;
        this.tds = tds;
        this.res = "";
        // Initialisation map
        mapOp.put("+", "ADD");
        mapOp.put("-", "SUB");
        mapOp.put("*", "MUL");
        mapOp.put("/", "DIV");
    }

 // test si un caractere est une idf
 	public boolean estLettre(Character c) {
 		if (c != null) {
 			int codeASCII = (int) c;
 			if ((codeASCII >= 97 && codeASCII < 122)
 					|| (codeASCII >= 65 && codeASCII <= 90)) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	 // test si un caractere est une constante

 	public static boolean estChiffre(String string) {
		try {
			Integer.parseInt(string + "");
			return true;

		} catch (NumberFormatException e) {
			// TODO: handle exception
			return false;
		}

	}
 	
    /**
     * Test si c'est un opérateur de calcul
     *
     * @param str Valeur du Noeud
     * @return boolean vrai ou faux
     */
 	public boolean estOperateur(String str){
        return str.equals("+") || str.equals("-") || str.equals("*") || str.equals("/");
 	}

    /**
     * Test si c'est un opérateur de calcul
     *
     * @param str Valeur du Noeud
     * @return boolean vrai ou faux
     */
    public boolean estOperateurBool(String str){
        return str.equals("<") || str.equals("<=") || str.equals(">=") || str.equals(">") || str.equals("==");
    }

    /**
     * Génération du code assembleur pour tout le programme.
     */
    public String generer_prog() {
        // Initialisation
        res += ".include beta.uasm\n"
                + "CMOVE(pile,SP)\n"
                + "BR(debut)\n";
        // Génération des DATA
        generer_data();
        // Début
        res += "debut:\n"
                + "\tCALL(main)\n"
                + "\tHALT()\n";
        //Génération du code
        generer_code();
        // Fin
        res += "pile:\n";
        return res;
    }

    /**
     * Génération du code assembleur pour les variables globales.
     */
    public void generer_data() {
        // Parcours de la Table des Symboles
        Set set = this.tds.getTds().keySet();
        Iterator it = set.iterator();
        while (it.hasNext()) {
            // Récupération de la Variable (IDF + SCOPE)
            Variable var = (Variable) it.next();
            if (var.getScope() == 0) { // Si c'est une variable globale
                // Si c'est un INT
                HashMap<String, String> mapTmp = this.tds.getTds().get(var);
                if (mapTmp.get("type").equals("int") && mapTmp.containsKey("valeur")) {
                    res += "\t" + var.getIdf() + ":LONG(" + this.tds.getTds().get(var).get("valeur") + ")\n";
                }
            }
        }
    }

    /**
     * Génération du code assembleur pour le code.
     */
    public void generer_code() {
        // Récupération de la racine de l'arbre
        Noeud racine = this.arbre.getRacine();
        // Parcours de tous les fils de l'arbre
        racine.getFils().stream().forEach((fils) -> {
            // Génération de la fonction
            generer_fonction(fils);
        });
    }

    /**
     * Génération du code assembleur d'une fonction.
     *
     * @param fils Noeud
     */
    public void generer_fonction(Noeud fils) {
        // Initialisation (label)
        fonctionCourante = fils.getValeur();
        // Recherche du nombre de variables locales
        HashMap<String, String> map = tds.rechercher(fils.getValeur());
        int nb_var_loc = new Integer(map.get("nombre_local"));
        res += fils.getValeur() + ":\n";
        res += "\tPUSH(LP)\n"
             + "\tPUSH(BP)\n"
             + "\tMOVE(SP, BP)\n"
             + "\tALLOCATE(" + nb_var_loc + ")\n";
        // Parcours de chaque fils du Noeud
        for(int i=0;i<fils.getFils().size();i++) {
            generer_instruction(fils.getFils().get(fils.getFils().size() - i - 1));
        }
        // Fin de la fonction
        res += "ret_" + fils.getValeur() + ":\n"
             + "\tDEALLOCATE(" + nb_var_loc + ")\n"
             + "\tPOP(BP)\n"
             + "\tPOP(LP)\n"
             + "\tRTN()\n";
    }

    /**
     * Génération du code assembleur d'une instruction.
     *
     * @param fils2 Noeud.
     */
    public void generer_instruction(Noeud fils) {
        switch (fils.getType()) {
            // Cas d'une affectation
            case "AFFECT":
            	generer_affectation(fils);
                break;
            case "RETURN":
                generer_return(fils);
                break;
            case "CALL":
                generer_call(fils);
                break;
            case "IF":
                generer_condition(fils);
                break;
            case "WHILE":
                generer_iteration(fils);
                break;
        }
    }

    /**
     * Génération du code assembleur d'un bloc
     *
     * @param fils Noeud
     */
    public void generer_bloc(Noeud fils) {
        // Parcours des Noeuds
        for(Noeud fils2 : fils.getFils()) {
            generer_instruction(fils2);
        }
    }

    /**
     * Génération du code assembleur d'une itération
     *
     * @param fils Noeud
     */
    public void generer_iteration(Noeud fils) {
        // Init while
        res += "while:\n";
        // Génération de l'expression
        generer_expression(fils.getFils().get(0));
        res += "\tPOP(r0)\n";
        res += "\tBF(r0, fwhile)\n";
        generer_bloc(fils.getFils().get(1));
        res += "\tBR(while)\n";
        // fwhile
        res += "fwhile:\n";
    }

    /**
     * Génération du code assembleur d'une condition
     *
     * @param fils Noeud
     */
    public void generer_condition(Noeud fils) {
        // Init si
        res += "si:\n";
        // Génération de l'expression
        generer_expression(fils.getFils().get(0));
        res += "\tPOP(r0)\n";
        res += "\tBF(r0, sinon)\n";
        generer_bloc(fils.getFils().get(1));
        res += "\tBR(fsi)\n";
        res += "sinon:\n";
        // Si y'a un else
        if(fils.getFils().size() == 3) {
            generer_bloc(fils.getFils().get(2));
        }
        // Fsi
        res += "fsi:\n";
    }

    /**
     * Génération du code assembleur d'un call
     *
     * @param fils Noeud
     */
    public void generer_call(Noeud fils) {
        // Récupération de nombre d'arguments
        HashMap<String, String> map = tds.rechercher(fils.getValeur());
        int nb_param = new Integer(map.get("nombre_argument"));
        res += "\tALLOCATE(" + nb_param + ")\n";
        // On met chaque paramètre dans la pile
        for (int i = 0; i < nb_param; i++) {
			generer_expression(fils.getFils().get(i));
		}
        res += "\tCALL(" + fils.getValeur() + ")\n"
             + "\tDEALLOCATE(" + nb_param + ")\n";
    }

    /**
     * Génération du code assembleur d'un return
     *
     * @param fils Noeud
     */
    public void generer_return(Noeud fils) {
        // Génération de l'expression du fils
        generer_expression(fils.getFils().get(0));
        // Recherche du nombre de paramètres
        HashMap<String, String> map = tds.rechercher(fonctionCourante);
        int nb_param = new Integer(map.get("nombre_argument"));
        res += "\tPOP(r0)\n"
             + "\tPUTFRAME((3+" + nb_param + ")*(-4), r0)\n"
             + "\tBR(ret_" + fonctionCourante + ")\n";
    }

    /**
     * Génération du code assembleur d'une affectation.
     *
     * @param fils Noeud.
     */
    public void generer_affectation(Noeud fils) {
        // Génération de l'expression du fils DROIT
        generer_expression(fils.getFils().get(1));
        // Affectation
        res += "\tPOP(r0)\n";
        int index = new Integer(fils.getFils().get(0).getValeur());
        Variable var = tds.getVariableWithIndex(index);
        // Cas variable globale
        if(var.getScope() == 0) {
            res += "\tST(r0, "+var.getIdf()+")\n";
        } else {
            int rang = new Integer(tds.getTds().get(var).get("rang"));
            res += index+"\tPUTFRAME(r0, " + (rang + 1) * 4 + ")\n";
        }
                
    }

    /**
     * Génération d'une expression.
     *
     * @param noeud Noeud
     */
    public void generer_expression(Noeud noeud) {
        // Test sur le type du Noeud
        switch(noeud.getType()) {
            case "CALL":
                generer_call(noeud);
                break;
            case "+":
            case "-":
            case "*":
            case "/":
                // Génération des expressions
                generer_expression(noeud.getFils().get(1));
                generer_expression(noeud.getFils().get(0));
                res+="\tPOP(r2)\n"
                    +"\tPOP(r1)\n"
                    + "\t" + mapOp.get(noeud.getType()) + "(r1, r2, r3)\n"
                    + "\tPUSH(r3)\n";
                break;
            case ">":
            case ">=":
            case "<":
            case "<=":
            case "==":
                generer_expression(noeud.getFils().get(0));
                generer_expression(noeud.getFils().get(1));
                res += "\tPOP(r1)\n"
                    + "\tPOP(r2)\n";
                // Test sur l'opérateur booléen
                if(noeud.getType().equals(">")) {
                    res += "\tCMPLT(r2, r1, r0)\n";
                }
                if(noeud.getType().equals(">=")) {
                    res += "\tCMPLE(r2, r1, r0)\n";
                }
                if(noeud.getType().equals("<")) {
                    res += "\tCMPLT(r1, r2, r0)\n";
                }
                if(noeud.getType().equals("<=")) {
                    res += "\tCMPLE(r1, r1, r0)\n";
                }
                if(noeud.getType().equals("==")) {
                    res += "\tCMPLT(r1, r2, r0)\n";
                }
                res += "\tPUSH(r0)\n";
                break;
            default:
                // Test sur la valeur
                String tmp = "";
                try {
                    int nb = new Integer(noeud.getValeur());
                    // C'est un nombre!
                    res += "\tCMOVE(" + noeud.getValeur() + ", r0)\n";
                } catch(NumberFormatException ex) {
                    // Ce n'est pas un nombre!
                    // Si c'est une variable globale
                    res += "\tLD(" + noeud.getValeur() + ", r0)\n";
                    // Si c'est une variable locale

                    // Si c'est un paramètre

                    // TODO
                } finally {
                    res += "\tPUSH(r0)\n";
                }
                break;
        }
 
    }


    public String toString() {
        return res;
    }
}
