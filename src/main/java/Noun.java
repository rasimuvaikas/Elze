/**
 * A class that stores a noun and all its relevant morphological information
 */

public class Noun
{
    String noun;
    String lemma;
    String gender;
    String number;
    String decl;

    public Noun()
    {
        noun = "";
        lemma = "";
        gender = "";
        number = "";
        decl = "";
    }

    public Noun(String n, String l, String g, String num, String d)
    {
        noun = n;
        lemma = l;
        gender = g;
        number = num;
        decl = d;
    }

    public String getNoun() {
        return noun;
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

    public String getLemma()
    {
        return lemma;
    }
}
