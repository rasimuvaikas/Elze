/**
 * A class that stores a pronoun and all its relevant morphological information
 */

public class Pronoun {

    String pronoun;
    String lemma;
    String gender;
    String number;
    String decl;

    public Pronoun() {
        this.pronoun = "";
        this.lemma = "";
        this.gender = "";
        this.number = "";
        this.decl = "";
    }

    public Pronoun(String pronoun, String lemma, String gender, String number, String decl) {
        this.pronoun = pronoun;
        this.lemma = lemma;
        this.gender = gender;
        this.number = number;
        this.decl = decl;
    }

    public String getPronoun() {
        return pronoun;
    }

    public String getLemma() {
        return lemma;
    }

    public String getGender() {
        return gender;
    }

    public String getNumber() {
        return number;
    }

    public String getDecl() {
        return decl;
    }
}
