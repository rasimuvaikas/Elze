/**
 * A class that stores a verb and all its relevant morphological information
 */


public class Verb {

    String verb;
    String lemma;
    boolean rflx;
    String tense;
    String person;
    String mood;

    public Verb() {
        verb = "";
        lemma = "";
        rflx = false;
        tense = "";
        person = "";
        mood = "";
    }

    public Verb(String v, String l, boolean r, String m, String t, String p) {
        verb = v;
        lemma = l;
        rflx = r;
        mood = m;
        tense = t;
        person = p;
    }

    public String getVerb() {
        return verb;
    }

    public String getLemma() {
        return lemma;
    }

    public boolean getRflx() {
        return rflx;
    }

    public String getTense() {
        return tense;
    }

    public String getPerson() {
        return person;
    }

    public void setVerb(String v) {
        verb = v;
    }

    public String getInf() {
        return lemma.substring(0, lemma.indexOf("("));
    }

    public String getMood() {
        return mood;
    }
}
