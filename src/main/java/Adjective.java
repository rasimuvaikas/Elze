/**
 * A class that stores an adjective and all its relevant morphological information
 */

public class Adjective
{
    String adj;
    String lemma;
    String gender;
    String number;
    String decl;

    public Adjective()
    {
        adj = "";
        lemma = "";
        gender = "";
        number = "";
        decl = "";
    }

    public Adjective(String a, String l, String g, String n, String d)
    {
        adj = a;
        lemma = l;
        gender = g;
        number = n;
        decl = d;
    }

    public String getAdj() {
        return adj;
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
