import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

public class Conjugator {

    Noun noun = null;
    Verb word = null;
    Adjective adj = null;
    Pronoun pn = null;
    ArrayList<String> lines;

    /**
     * Take a string of words, write them into a txt file, analyse them with with a morphological annotator
     * accessed through python script, throw that into an out.txt
     * @param s the string of words
     * @throws IOException
     */
    public Conjugator(String s) throws IOException, URISyntaxException {

        File file = new File("src/main/resources/analyse.txt");
        PrintWriter pw = new PrintWriter(file, "UTF-8");
        pw.println(s);
        pw.close();


        Process process = Runtime.getRuntime().exec("python src/main/java/teksto_lemavimas.py src/main/resources/analyse src/main/resources/out");
        BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        String line;
        while ((line = stdError.readLine()) != null) {
            System.out.println(line);
        }
        fileToList();
    }

    /**
     * Put the content of the output files that the annotator produced into a list
     * @return a list of lines from the text file the annotator produced
     * @throws IOException
     * @throws FileNotFoundException
     */

    private ArrayList<String> fileToList() throws IOException, FileNotFoundException, URISyntaxException {

        BufferedReader iS = new BufferedReader(new InputStreamReader(new FileInputStream(new File("src/main/resources/out.txt"))));
        String line;
        lines = new ArrayList<>();
        while ((line = iS.readLine()) != null) {
            lines.add(line);
        }
        return lines;
    }

    /**
     * Check if a verb is reflexive
     * @param s sngr. or nsngr. depending on whether the verb is reflexive or not
     * @return true if the verb is reflexive
     */
    private boolean isReflexive(String s) {
        if (s.equals("sngr.")) {
            return true;
        }
        return false;
    }

    /**
     * Find the pronoun in the text
     * @return a Pronoun object
     */
    public Pronoun findPronoun() {
        ArrayList<String> discard = new ArrayList<>(Arrays.asList("tu", "aš", "mes"));
        for (String s : lines) {
            String forNow = "";
            String[] temp = s.trim().split("\\s+");
            for (int i = 2; i < temp.length; i++) {
                forNow += temp[i];
            }
            String[] timp = forNow.trim().split(",");

            if (timp[0].equals("įv.") && !discard.contains(temp[1])) {
                pn = new Pronoun(temp[0], temp[1], timp[1], timp[2], timp[3]);
            }
        }
        return pn;
    }

    /**
     * Find the adjective in the text
     * @return an Adjective object
     */
    public Adjective findAdjective() {
        for (String s : lines) {
            String forNow = "";
            String[] temp = s.trim().split("\\s+");
            for (int i = 2; i < temp.length; i++) {
                forNow += temp[i];
            }
            String[] timp = forNow.trim().split(",");

            if (timp[0].equals("bdv.")) {
                adj = new Adjective(temp[0], temp[1], timp[4], timp[5], timp[6]);
            }
        }
        return adj;
    }

    /**
     * Find the noun in the text
     * @return a Noun object
     */
    public Noun findNoun() {
        for (String s : lines) {
            String forNow = "";
            String[] temp = s.trim().split("\\s+");
            for (int i = 2; i < temp.length; i++) {
                forNow += temp[i];
            }
            String[] timp = forNow.trim().split(",");

            if (timp[0].equals("dkt.")) {
                noun = new Noun(temp[0], temp[1], timp[1], timp[2], timp[3]);
            }
        }
        return noun;
    }

    /**
     * Find the verb in the text
     * @return a Verb object
     */
    public Verb findVerb() {
        for (String s : lines) {
            String forNow = "";
            String[] temp = s.trim().split("\\s+");
            for (int i = 2; i < temp.length; i++) {
                forNow += temp[i];
            }
            String[] timp = forNow.trim().split(",");

            if (timp[0].equals("vksm.") && timp.length > 6) {
                word = new Verb(temp[0], temp[1], isReflexive(timp[2]), timp[3], timp[4], timp[6]);
            } else if (timp[0].equals("vksm.") && timp.length < 7) {
                word = new Verb(temp[0], temp[1], isReflexive(timp[2]), timp[3], "none", timp[5]);
            }
        }

        return word;
    }

    /**
     * Conjugate a verb in 1st person to 2nd person singular, and a verb from 2nd person singular into 1st person singular
     * All moods and tenses
     * @param v the verb object that contains the verb to be conjugated
     * @return a conjugated verb as a String
     */
    public String conjugateOppo(Verb v) {
        String cnjg = v.getLemma().substring(v.getLemma().indexOf("(") + 1, v.getLemma().length() - 1);
        String vksm = v.getVerb();


        if (v.getMood().equals("tar.n.")) {
            if (v.getPerson().equals("1asm.")) {
                if (vksm.endsWith("si") && v.getRflx()) {
                    vksm = vksm.substring(0, vksm.length() - 6) + "tumeisi";
                } else {
                    vksm = vksm.substring(0, vksm.length() - 4) + "tum";
                }
            }

            if (v.getPerson().equals("2asm.") || v.getPerson().equals("3asm.")) {
                if (vksm.endsWith("si")) {
                    vksm = vksm.substring(0, vksm.length() - 7) + "čiausi";
                } else {
                    vksm = vksm.substring(0, vksm.length() - 3) + "čiau";
                }
            }
        }

        if (v.getMood().equals("tiesiog.n.")) {
            if (v.getTense().equals("es.l.")) {
                if (v.getPerson().equals("1asm.")) {
                    if (vksm.endsWith("si") && v.getRflx()) {
                        if (cnjg.equals("-disi,-dėjosi")) {
                            vksm = vksm.substring(0, vksm.length() - 6) + "iesi";
                        }

                        if (cnjg.equals("-čiasi,-tėsi")) {
                            vksm = vksm.substring(0, vksm.length() - 6) + "tiesi";
                        }

                        if (vksm.endsWith("iuosi")) {
                            vksm = vksm.substring(0, vksm.length() - 5) + "iesi";
                        }

                        if (vksm.endsWith("uosi")) {
                            vksm = vksm.substring(0, vksm.length() - 4) + "iesi";
                        }

                        if (vksm.endsWith("ausi")) {
                            vksm = vksm.substring(0, vksm.length() - 4) + "aisi";
                        }

                    }

                    if (cnjg.equals("-di,-dėjo")) {
                        vksm = vksm.substring(0, vksm.length() - 3) + "i";
                    }

                    if (cnjg.equals("-čia,-tė") || cnjg.equals("-enčia,-entė")) {
                        vksm = vksm.substring(0, vksm.length() - 3) + "ti";
                    }

                    if (vksm.endsWith("iu")) {
                        vksm = vksm.substring(0, vksm.length() - 2) + "i";
                    }

                    if (vksm.endsWith("u")) {
                        vksm = vksm.substring(0, vksm.length() - 1) + "i";
                    }

                    if (vksm.endsWith("au")) {
                        vksm = vksm.substring(0, vksm.length() - 2) + "ai";
                    }
                }

                //the annotator often recognises verbs in 2nd person as 3rd person
                if (v.getPerson().equals("2asm.") || v.getPerson().equals("3asm."))
                {
                    if (vksm.endsWith("si") && v.getRflx()) {
                        if (cnjg.equals("-disi,-dėjosi")) {
                            vksm = vksm.substring(0, vksm.length() - 4) + "žiuosi";
                        }

                        if (cnjg.equals("-čiasi,-tėsi")) {
                            vksm = vksm.substring(0, vksm.length() - 5) + "čiuosi";
                        }


                        if (cnjg.equals("-asi,-osi")) {
                            vksm = vksm.substring(0, vksm.length() - 4) + "uosi";
                        }

                        if (cnjg.equals("-iasi,-ėsi")) {
                            vksm = vksm.substring(0, vksm.length() - 4) + "iuosi";
                        }

                        if (vksm.endsWith("aisi")) {
                            vksm = vksm.substring(0, vksm.length() - 4) + "ausi";
                        }
                    }

                    if (cnjg.equals("-di,-dėjo")) {
                        vksm = vksm.substring(0, vksm.length() - 1) + "žiu";
                    }

                    if (cnjg.equals("-čia,-tė") || cnjg.equals("-enčia,-entė")) {
                        vksm = vksm.substring(0, vksm.length() - 2) + "čiu";
                    }

                    if (cnjg.equals("-a,-ėjo")) {
                        vksm = vksm.substring(0, vksm.length() - 1) + "u";
                    }

                    if (cnjg.equals("-a,-o")) {
                        vksm = vksm.substring(0, vksm.length() - 1) + "u";
                    }

                    if (cnjg.equals("-ia,-ė")) {
                        vksm = vksm.substring(0, vksm.length() - 1) + "iu";
                    }

                    if (cnjg.equals("-to,-tė")) {
                        vksm = vksm.substring(0, vksm.length() - 1) + "au";
                    }

                    if (cnjg.equals("-i,-ėjo")) {
                        vksm = vksm.substring(0, vksm.length() - 1) + "iu";
                    }

                    if (cnjg.equals("-o,-ė")) {
                        vksm = vksm.substring(0, vksm.length() - 2) + "au";
                    }

                    if (cnjg.equals("-nta,-to")) {
                        vksm = vksm.substring(0, vksm.length() - 1) + "u";
                    }

                    if(cnjg.equals("-ja,-jo")) {
                        vksm = vksm.substring(0, vksm.length() - 1) + "u";
                    }
                }

            } else if (v.getTense().equals("būt.k.l.")) {
                if (v.getPerson().equals("1asm.")) {
                    if (vksm.endsWith("si") && v.getRflx()) {
                        if (cnjg.equals("-disi,-dėjosi")) {
                            vksm = vksm.substring(0, vksm.length() - 4) + "aisi";
                        }

                        if (cnjg.equals("-čiasi,-tėsi")) {
                            vksm = vksm.substring(0, vksm.length() - 6) + "teisi";
                        }

                        if (vksm.endsWith("iausi")) {
                            vksm = vksm.substring(0, vksm.length() - 5) + "eisi";
                        }

                        if (vksm.endsWith("ausi")) {
                            vksm = vksm.substring(0, vksm.length() - 4) + "aisi";
                        }

                    }

                    if (cnjg.equals("-di,-dėjo")) {
                        vksm = vksm.substring(0, vksm.length() - 1) + "i";
                    }

                    if (cnjg.equals("-čia,-tė") || cnjg.equals("-enčia,-entė")) {
                        vksm = vksm.substring(0, vksm.length() - 4) + "tei";
                    }

                    if (vksm.endsWith("ėjau")) {
                        vksm = vksm.substring(0, vksm.length() - 4) + "ėjai";
                    }

                    if (vksm.endsWith("čiau")) {
                        vksm = vksm.substring(0, vksm.length() - 4) + "tei";
                    }

                    if (cnjg.equals("-o,-ė")) {
                        vksm = vksm.substring(0, vksm.length() - 3) + "ei";
                    }

                    if (vksm.endsWith("au")) {
                        vksm = vksm.substring(0, vksm.length() - 1) + "i";
                    }


                }
                //the annotator often recognises verbs in 2nd person as 3rd person
                if (v.getPerson().equals("2asm.") || v.getPerson().equals("3asm.")) {
                    if (vksm.endsWith("si") && v.getRflx()) {
                        if (cnjg.equals("-disi,-dėjosi")) {
                            vksm = vksm.substring(0, vksm.length() - 4) + "ausi";
                        }

                        if (cnjg.equals("-čiasi,-tėsi")) {
                            vksm = vksm.substring(0, vksm.length() - 5) + "čiausi";
                        }


                        if (cnjg.equals("-asi,-osi")) {
                            vksm = vksm.substring(0, vksm.length() - 4) + "ausi";
                        }

                        if (cnjg.equals("-iasi,-ėsi")) {
                            vksm = vksm.substring(0, vksm.length() - 4) + "iausi";
                        }

                        if (vksm.endsWith("eisi")) {
                            vksm = vksm.substring(0, vksm.length() - 4) + "iausi";
                        }
                    }

                    if (cnjg.equals("-di,-dėjo")) {
                        vksm = vksm.substring(0, vksm.length() - 1) + "u";
                    }

                    if (cnjg.equals("-čia,-tė") || cnjg.equals("-enčia,-entė")) {
                        vksm = vksm.substring(0, vksm.length() - 3) + "čiau";
                    }

                    if (cnjg.equals("-a,-ėjo")) {
                        vksm = vksm.substring(0, vksm.length() - 1) + "u";
                    }

                    if (cnjg.equals("-a,-o")) {
                        vksm = vksm.substring(0, vksm.length() - 1) + "u";
                    }

                    if (cnjg.equals("-ia,-ė")) {
                        vksm = vksm.substring(0, vksm.length() - 2) + "iau";
                    }

                    if (cnjg.equals("-to,-tė")) {
                        vksm = vksm.substring(0, vksm.length() - 3) + "čiau";
                    }

                    if (cnjg.equals("-i,-ėjo")) {
                        vksm = vksm.substring(0, vksm.length() - 1) + "u";
                    }

                    if (cnjg.equals("-o,-ė")) {
                        if (vksm.endsWith("tei")) {
                            vksm = vksm.substring(0, vksm.length() - 3) + "čiau";
                        } else {
                            vksm = vksm.substring(0, vksm.length() - 2) + "iau";
                        }
                    }

                    if (cnjg.equals("-nta,-to")) {
                        vksm = vksm.substring(0, vksm.length() - 1) + "u";
                    }

                    if(cnjg.equals("-ja,-jo")) {
                        vksm = vksm.substring(0, vksm.length() - 1) + "u";
                    }
                }

            } else if (v.getTense().equals("būt.d.l.")) {
                if (v.getPerson().equals("1asm.")) {
                    if (vksm.endsWith("si") && v.getRflx()) {
                        vksm = vksm.substring(0, vksm.length() - 4) + "aisi";
                    } else {
                        vksm = vksm.substring(0, vksm.length() - 1) + "i";
                    }
                }
                //the annotator often recognises verbs in 2nd person as 3rd person
                if (v.getPerson().equals("2asm.") || v.getPerson().equals("3asm.")) {
                    if (vksm.endsWith("si") && v.getRflx()) {
                        vksm = vksm.substring(0, vksm.length() - 4) + "aisi";
                    } else {
                        vksm = vksm.substring(0, vksm.length() - 1) + "u";
                    }

                }
            } else if (v.getTense().equals("būs.l.")) {
                if (v.getPerson().equals("1asm.")) {
                    if (vksm.endsWith("si") && v.getRflx()) {
                        vksm = vksm.substring(0, vksm.length() - 5) + "iesi";
                    } else {
                        vksm = vksm.substring(0, vksm.length() - 2) + "i";
                    }
                }
                //the annotator often recognises verbs in 2nd person as 3rd person
                if (v.getPerson().equals("2asm.") || v.getPerson().equals("3asm.")) {
                    if (vksm.endsWith("si") && v.getRflx()) {
                        vksm = vksm.substring(0, vksm.length() - 4) + "iuosi";
                    } else {
                        vksm = vksm.substring(0, vksm.length() - 1) + "iu";
                    }

                }
            }
        }
        return vksm;
    }

    /**
     * Conjugate a verb into its plural 1st person form
     * @param v the verb object that contains the verb to be conjugated
     * @return the conjugated verb
     */
    public String conjugatePl(Verb v) {
        String cnjg = v.getLemma().substring(v.getLemma().indexOf("(") + 1, v.getLemma().length() - 1);
        String vksm = v.getVerb();

        if (v.getTense().equals("es.l.")) {
            if (v.getPerson().equals("1asm.")) {
                if (vksm.endsWith("si") && v.getRflx()) {

                    if (cnjg.equals("-disi,-dėjosi")) {
                        vksm = vksm.substring(0, vksm.length() - 6) + "imės";
                    }

                    if (cnjg.equals("-čiasi,-tėsi")) {
                        vksm = vksm.substring(0, vksm.length() - 6) + "iamės";
                    }
                    if (vksm.endsWith("iuosi")) {
                        vksm = vksm.substring(0, vksm.length() - 5) + "imės";
                    }

                    if (vksm.endsWith("uosi")) {
                        vksm = vksm.substring(0, vksm.length() - 4) + "amės";
                    }

                    if (vksm.endsWith("ausi")) {
                        vksm = vksm.substring(0, vksm.length() - 4) + "omės";
                    }

                }

                if (cnjg.equals("-di,-dėjo")) {
                    vksm = vksm.substring(0, vksm.length() - 3) + "ime";
                }

                if (cnjg.equals("-čia,-tė") || cnjg.equals("-enčia,-entė")) {
                    vksm = vksm.substring(0, vksm.length() - 2) + "iame";
                }

                if (vksm.endsWith("iu") && cnjg.equals("-ia,-ė")) {
                    vksm = vksm.substring(0, vksm.length() - 2) + "iame";
                }

                if (vksm.endsWith("iu")) {
                    vksm = vksm.substring(0, vksm.length() - 2) + "ime";
                }

                if (vksm.endsWith("au")) {
                    vksm = vksm.substring(0, vksm.length() - 2) + "ome";
                }

                if (vksm.endsWith("u")) {
                    vksm = vksm.substring(0, vksm.length() - 1) + "ame";
                }

            }
        } else if (v.getTense().equals("būt.k.l.")) {
            if (v.getPerson().equals("1asm.")) {
                if (vksm.endsWith("si") && v.getRflx()) {
                    if (cnjg.equals("-disi,-dėjosi")) {
                        vksm = vksm.substring(0, vksm.length() - 4) + "omės";
                    }

                    if (cnjg.equals("-čiasi,-tėsi")) {
                        vksm = vksm.substring(0, vksm.length() - 6) + "tėmės";
                    }

                    if (vksm.endsWith("iausi")) {
                        vksm = vksm.substring(0, vksm.length() - 5) + "ėmės";
                    }

                    if (vksm.endsWith("ausi")) {
                        vksm = vksm.substring(0, vksm.length() - 4) + "omės";
                    }
                }

                if (cnjg.equals("-di,-dėjo")) {
                    vksm = vksm.substring(0, vksm.length() - 1) + "ome";
                }

                if (cnjg.equals("-čia,-tė")) {
                    vksm = vksm.substring(0, vksm.length() - 4) + "tėme";
                }

                if (vksm.endsWith("ėjau")) {
                    vksm = vksm.substring(0, vksm.length() - 4) + "ėjome";
                }

                if (vksm.endsWith("au")) {
                    vksm = vksm.substring(0, vksm.length() - 1) + "ome";
                }

                if (vksm.endsWith("čiau")) {
                    vksm = vksm.substring(0, vksm.length() - 4) + "tėme";
                }
            }
        } else if (v.getTense().equals("būt.d.l.")) {
            if (v.getPerson().equals("1asm.")) {
                if (vksm.endsWith("si") && v.getRflx()) {
                    vksm = vksm.substring(0, vksm.length() - 4) + "omės";
                } else {
                    vksm = vksm.substring(0, vksm.length() - 1) + "ome";
                }
            }
        } else if (v.getTense().equals("būs.l.")) {
            if (v.getPerson().equals("1asm.")) {
                if (vksm.endsWith("si") && v.getRflx()) {
                    vksm = vksm.substring(0, vksm.length() - 5) + "imės";
                } else {
                    vksm = vksm.substring(0, vksm.length() - 2) + "ime";
                }
            }
        }

        return vksm;

    }

    /**
     * Conjugate a verb into its subjunctive 1st person form
     * @param v the verb object that contains the verb to be conjugated
     * @return the conjugated verb
     */
    public String conjugateSubj(Verb v) {
        String cnjg = v.getLemma().substring(v.getLemma().indexOf("(") + 1, v.getLemma().length() - 1);
        String vksm = v.getVerb();

        if (v.getTense().equals("es.l.")) {
            if (vksm.endsWith("si") && v.getRflx()) {
                if (cnjg.equals("-disi,-dėjosi")) {
                    vksm = vksm.substring(0, vksm.length() - 4) + "ėčiausi";
                }

                if (cnjg.equals("-čiasi,-tėsi")) {
                    vksm = vksm.substring(0, vksm.length() - 5) + "sčiausi";
                }

                if (cnjg.equals("-asi,-osi")) {
                    vksm = vksm.substring(0, vksm.length() - 4) + "čiausi";
                }

                if (cnjg.equals("-iasi,-ėsi")) {
                    vksm = vksm.substring(0, vksm.length() - 4) + "čiausi";
                }

                if (vksm.endsWith("aisi")) {
                    vksm = vksm.substring(0, vksm.length() - 4) + "yčiausi";
                }

            }

            if (cnjg.equals("-di,-dėjo")) {
                vksm = vksm.substring(0, vksm.length() - 1) + "ėčiau";
            }

            if (cnjg.equals("-čia,-tė")) {
                vksm = vksm.substring(0, vksm.length() - 2) + "sčiau";
            }

            if (cnjg.equals("-a,-ėjo")) {
                vksm = vksm.substring(0, vksm.length() - 1) + "ėčiau";
            }

            if (cnjg.equals("-a,-o")) {
                vksm = vksm.substring(0, vksm.length() - 1) + "čiau";
            }

            if (cnjg.equals("-ia,-ė")) {
                vksm = vksm.substring(0, vksm.length() - 1) + "čiau";
            }

            if (cnjg.equals("-to,-tė")) {
                vksm = vksm.substring(0, vksm.length() - 2) + "yčiau";
            }

            if (cnjg.equals("-i,-ėjo")) {
                vksm = vksm.substring(0, vksm.length() - 1) + "ėčiau";
            }

            if (cnjg.equals("-o,-ė")) {
                vksm = vksm.substring(0, vksm.length() - 2) + "yčiau";
            }
        }

        return vksm;
    }

    /**
     * Decline a noun to a desired case
     * @param n    the noun object that contains the noun
     * @param rqrd the desired case
     * @return a declined noun
     */
    public String declineN(Noun n, String rqrd) {
        String noun = n.getNoun();
        String lemma = n.getLemma();

        if (n.getNumber().equals("vns.")) {
            if (n.getGender().equals("vyr.g.")) {
                if (rqrd.equals("V.")) {
                    noun = lemma;
                } else if (rqrd.equals("K.")) {
                    if (lemma.endsWith("as")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "o";
                    } else if (lemma.endsWith("is")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "io";
                    } else if (lemma.endsWith("ys")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "io";
                    } else if (lemma.endsWith("us")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "aus";
                    }
                } else if (rqrd.equals("N.")) {
                    if (lemma.endsWith("as")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "ui";
                    } else if (lemma.endsWith("is")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "iui";
                    } else if (lemma.endsWith("ys")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "iui";
                    } else if (lemma.endsWith("us")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "ui";
                    }
                } else if (rqrd.equals("G.")) {
                    if (lemma.endsWith("as")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "ą";
                    } else if (lemma.endsWith("is")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "į";
                    } else if (lemma.endsWith("ys")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "į";
                    } else if (lemma.endsWith("us")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "ų";
                    }
                } else if (rqrd.equals("Į.")) {
                    if (lemma.endsWith("as")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "u";
                    } else if (lemma.endsWith("is")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "iu";
                    } else if (lemma.endsWith("ys")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "iu";
                    } else if (lemma.endsWith("us")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "umi";
                    }
                } else if (rqrd.equals("Vt.")) {
                    if (lemma.endsWith("as")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "e";
                    } else if (lemma.endsWith("is")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "yje";
                    } else if (lemma.endsWith("ys")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "yje";
                    } else if (lemma.endsWith("us")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "uje";
                    }
                }
            } else {
                if (rqrd.equals("V.")) {
                    noun = lemma;
                } else if (rqrd.equals("K.")) {
                    if (lemma.endsWith("a")) {
                        noun = lemma.substring(0, lemma.length() - 1) + "os";
                    } else if (lemma.endsWith("ė")) {
                        noun = lemma.substring(0, lemma.length() - 1) + "ės";
                    } else if (lemma.endsWith("is")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "ies";
                    }
                } else if (rqrd.equals("N.")) {
                    if (lemma.endsWith("a")) {
                        noun = lemma.substring(0, lemma.length() - 1) + "ai";
                    } else if (lemma.endsWith("ė")) {
                        noun = lemma.substring(0, lemma.length() - 1) + "ei";
                    } else if (lemma.endsWith("is")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "iai";
                    }
                } else if (rqrd.equals("G.")) {
                    if (lemma.endsWith("a")) {
                        noun = lemma.substring(0, lemma.length() - 1) + "ą";
                    } else if (lemma.endsWith("ė")) {
                        noun = lemma.substring(0, lemma.length() - 1) + "ę";
                    } else if (lemma.endsWith("is")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "į";
                    }
                } else if (rqrd.equals("Į.")) {
                    if (lemma.endsWith("a")) {
                        noun = lemma.substring(0, lemma.length() - 1) + "a";
                    } else if (lemma.endsWith("ė")) {
                        noun = lemma.substring(0, lemma.length() - 1) + "e";
                    } else if (lemma.endsWith("is")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "imi";
                    }
                } else if (rqrd.equals("Vt.")) {
                    if (lemma.endsWith("a")) {
                        noun = lemma.substring(0, lemma.length() - 1) + "oje";
                    } else if (lemma.endsWith("ė")) {
                        noun = lemma.substring(0, lemma.length() - 1) + "ėje";
                    } else if (lemma.endsWith("is")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "yje";
                    }
                }
            }
        } else {
            if (n.getGender().equals("vyr.g.")) {
                if (rqrd.equals("V.")) {
                    if (lemma.endsWith("as")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "ai";
                    } else if (lemma.endsWith("is")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "iai";
                    } else if (lemma.endsWith("ys")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "iai";
                    } else if (lemma.endsWith("us")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "ūs";
                    }
                } else if (rqrd.equals("K.")) {
                    if (lemma.endsWith("as")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "ių";
                    } else if (lemma.endsWith("is")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "ių";
                    } else if (lemma.endsWith("ys")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "ių";
                    } else if (lemma.endsWith("us")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "ų";
                    }
                } else if (rqrd.equals("N.")) {
                    if (lemma.endsWith("as")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "ams";
                    } else if (lemma.endsWith("is")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "iams";
                    } else if (lemma.endsWith("ys")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "iams";
                    } else if (lemma.endsWith("ius")) {
                        noun = lemma.substring(0, lemma.length() - 3) + "iams";
                    } else if (lemma.endsWith("us")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "ums";
                    }
                } else if (rqrd.equals("G.")) {
                    if (lemma.endsWith("as")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "us";
                    } else if (lemma.endsWith("is")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "ius";
                    } else if (lemma.endsWith("ys")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "ius";
                    } else if (lemma.endsWith("us")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "us";
                    }
                } else if (rqrd.equals("Į.")) {
                    if (lemma.endsWith("as")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "ais";
                    } else if (lemma.endsWith("is")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "iais";
                    } else if (lemma.endsWith("ys")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "iais";
                    } else if (lemma.endsWith("ius")) {
                        noun = lemma.substring(0, lemma.length() - 3) + "iais";
                    } else if (lemma.endsWith("us")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "umis";
                    }
                } else if (rqrd.equals("Vt.")) {
                    if (lemma.endsWith("as")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "uose";
                    } else if (lemma.endsWith("is")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "iuose";
                    } else if (lemma.endsWith("ys")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "iuose";
                    } else if (lemma.endsWith("ius")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "uose";
                    } else if (lemma.endsWith("us")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "ūse";
                    }
                }
            } else {
                if (rqrd.equals("V.")) {
                    if (lemma.endsWith("a")) {
                        noun = lemma.substring(0, lemma.length() - 1) + "os";
                    } else if (lemma.endsWith("ė")) {
                        noun = lemma.substring(0, lemma.length() - 1) + "ės";
                    } else if (lemma.endsWith("is")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "ys";
                    }
                } else if (rqrd.equals("K.")) {
                    if (lemma.endsWith("a")) {
                        noun = lemma.substring(0, lemma.length() - 1) + "ų";
                    } else if (lemma.endsWith("ė")) {
                        noun = lemma.substring(0, lemma.length() - 1) + "ių";
                    } else if (lemma.endsWith("is")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "ų";
                    }
                } else if (rqrd.equals("N.")) {
                    if (lemma.endsWith("a")) {
                        noun = lemma.substring(0, lemma.length() - 1) + "oms";
                    } else if (lemma.endsWith("ė")) {
                        noun = lemma.substring(0, lemma.length() - 1) + "ėms";
                    } else if (lemma.endsWith("is")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "ims";
                    }
                } else if (rqrd.equals("G.")) {
                    if (lemma.endsWith("a")) {
                        noun = lemma.substring(0, lemma.length() - 1) + "as";
                    } else if (lemma.endsWith("ė")) {
                        noun = lemma.substring(0, lemma.length() - 1) + "es";
                    } else if (lemma.endsWith("is")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "is";
                    }
                } else if (rqrd.equals("Į.")) {
                    if (lemma.endsWith("a")) {
                        noun = lemma.substring(0, lemma.length() - 1) + "omis";
                    } else if (lemma.endsWith("ė")) {
                        noun = lemma.substring(0, lemma.length() - 1) + "ėmis";
                    } else if (lemma.endsWith("is")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "imis";
                    }
                } else if (rqrd.equals("Vt.")) {
                    if (lemma.endsWith("a")) {
                        noun = lemma.substring(0, lemma.length() - 1) + "ose";
                    } else if (lemma.endsWith("ė")) {
                        noun = lemma.substring(0, lemma.length() - 1) + "ėse";
                    } else if (lemma.endsWith("is")) {
                        noun = lemma.substring(0, lemma.length() - 2) + "yse";
                    }
                }
            }
        }
        return noun;
    }

    /**
     * Decline an adjective to a desired case
     * @param a    the adjective object that contains the adjective
     * @param rqrd the desired case
     * @return
     */
    public String declineA(Adjective a, String rqrd) {
        String adj = a.getAdj();
        String lemma = a.getLemma();

        if (a.getNumber().equals("vns.")) {
            if (a.getGender().equals("vyr.g.")) {
                if (rqrd.equals("V.")) {
                    adj = lemma;
                } else if (rqrd.equals("K.")) {
                    if (lemma.endsWith("as")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "o";
                    } else if (lemma.endsWith("is") || lemma.endsWith("ys")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "io";
                    } else if (lemma.endsWith("us")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "aus";
                    }
                } else if (rqrd.equals("N.")) {
                    if (lemma.endsWith("as")) {
                        adj = lemma.substring(0, lemma.length() - 1) + "m";
                    } else if (lemma.endsWith("is") || lemma.endsWith("ys")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "iam";
                    } else if (lemma.endsWith("us")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "iam";
                    }
                } else if (rqrd.equals("G.")) {
                    if (lemma.endsWith("as")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "ą";
                    } else if (lemma.endsWith("is") || lemma.endsWith("ys")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "į";
                    } else if (lemma.endsWith("us")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "ų";
                    }
                } else if (rqrd.equals("Į.")) {
                    if (lemma.endsWith("as")) {
                        adj = lemma.substring(0, lemma.length() - 1) + "u";
                    } else if (lemma.endsWith("is") || lemma.endsWith("ys")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "iu";
                    } else if (lemma.endsWith("us")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "iu";
                    }
                } else if (rqrd.equals("Vt.")) {
                    if (lemma.endsWith("as")) {
                        adj = lemma.substring(0, lemma.length() - 1) + "me";
                    } else if (lemma.endsWith("is") || lemma.endsWith("ys")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "iame";
                    } else if (lemma.endsWith("us")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "iame";
                    }
                }
            } else {
                if (rqrd.equals("V.")) {
                    adj = lemma;
                } else if (rqrd.equals("K.")) {
                    if (lemma.endsWith("as")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "os";
                    } else if (lemma.endsWith("us")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "ios";
                    } else if (lemma.endsWith("is")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "ės";
                    }
                } else if (rqrd.equals("N.")) {
                    if (lemma.endsWith("as")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "ai";
                    } else if (lemma.endsWith("us")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "iai";
                    } else if (lemma.endsWith("is")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "ei";
                    }
                } else if (rqrd.equals("G.")) {
                    if (lemma.endsWith("as")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "ą";
                    } else if (lemma.endsWith("us")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "ią";
                    } else if (lemma.endsWith("is")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "ę";
                    }
                } else if (rqrd.equals("Į.")) {
                    if (lemma.endsWith("as")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "a";
                    } else if (lemma.endsWith("us")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "ia";
                    } else if (lemma.endsWith("is")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "e";
                    }
                } else if (rqrd.equals("Vt.")) {
                    if (lemma.endsWith("a")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "oje";
                    } else if (lemma.endsWith("i")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "ioje";
                    } else if (lemma.endsWith("ė")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "ėje";
                    }
                }
            }
        } else {
            if (a.getGender().equals("vyr.g.")) {
                if (rqrd.equals("V.")) {
                    if (lemma.endsWith("as")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "i";
                    } else if (lemma.endsWith("is") || lemma.endsWith("ys")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "iai";
                    } else if (lemma.endsWith("us")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "ūs";
                    }
                } else if (rqrd.equals("K.")) {
                    if (lemma.endsWith("as")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "ų";
                    } else if (lemma.endsWith("is") || lemma.endsWith("ys")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "ių";
                    } else if (lemma.endsWith("us")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "ių";
                    }
                } else if (rqrd.equals("N.")) {
                    if (lemma.endsWith("as")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "iems";
                    } else if (lemma.endsWith("is") || lemma.endsWith("ys")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "iems";
                    } else if (lemma.endsWith("us")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "iems";
                    }
                } else if (rqrd.equals("G.")) {
                    if (lemma.endsWith("as")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "us";
                    } else if (lemma.endsWith("is") || lemma.endsWith("ys")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "ius";
                    } else if (lemma.endsWith("us")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "ius";
                    }
                } else if (rqrd.equals("Į.")) {
                    if (lemma.endsWith("as")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "ais";
                    } else if (lemma.endsWith("is") || lemma.endsWith("ys")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "iais";
                    } else if (lemma.endsWith("us")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "iais";
                    }
                } else if (rqrd.equals("Vt.")) {
                    if (lemma.endsWith("as")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "uose";
                    } else if (lemma.endsWith("is") || lemma.endsWith("ys")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "iuose";
                    } else if (lemma.endsWith("us")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "iuose";
                    }
                }
            } else {
                if (rqrd.equals("V.")) {
                    if (lemma.endsWith("as")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "os";
                    } else if (lemma.endsWith("us")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "ios";
                    } else if (lemma.endsWith("is")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "ės";
                    }
                } else if (rqrd.equals("K.")) {
                    if (lemma.endsWith("as")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "ų";
                    } else if (lemma.endsWith("us")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "ių";
                    } else if (lemma.endsWith("is")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "ių";
                    }
                } else if (rqrd.equals("N.")) {
                    if (lemma.endsWith("as")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "oms";
                    } else if (lemma.endsWith("us")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "ioms";
                    } else if (lemma.endsWith("is")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "ėms";
                    }
                } else if (rqrd.equals("G.")) {
                    if (lemma.endsWith("as")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "as";
                    } else if (lemma.endsWith("us")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "ias";
                    } else if (lemma.endsWith("is")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "es";
                    }
                } else if (rqrd.equals("Į.")) {
                    if (lemma.endsWith("as")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "omis";
                    } else if (lemma.endsWith("us")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "iomis";
                    } else if (lemma.endsWith("is")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "ėmis";
                    }
                } else if (rqrd.equals("Vt.")) {
                    if (lemma.endsWith("as")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "ose";
                    } else if (lemma.endsWith("us")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "iose";
                    } else if (lemma.endsWith("is")) {
                        adj = lemma.substring(0, lemma.length() - 2) + "ėse";
                    }
                }
            }
        }

        return adj;
    }

    /**
     * Decline a pronoun object to a desired case
     * @param p    the pronoun object that contains the pronoun
     * @param rqrd the desired case
     * @return a declined pronoun
     */
    public String declineP(Pronoun p, String rqrd) {
        String pn = p.getPronoun();
        String lemma = p.getLemma();
        ArrayList<String> par1 = new ArrayList<>(Arrays.asList("tas", "šitas", "anas", "katras", "kas"));
        ArrayList<String> par2 = new ArrayList<>(Arrays.asList("kitas", "visas", "vienas", "kiekvienas",
                "tam tikras", "manas", "tavas", "savas"));


        if (p.getNumber().equals("vns.")) {
            if (p.getGender().equals("vyr.g.")) {
                if (rqrd.equals("V.")) {
                    pn = lemma;
                } else if (rqrd.equals("K.")) {
                    if (par1.contains(lemma)) {
                        pn = lemma.substring(0, lemma.length() - 2) + "o";
                    } else if (par2.contains(lemma)) {
                        pn = lemma.substring(0, lemma.length() - 2) + "o";
                    } else if (lemma.endsWith("oks")) {
                        pn = lemma.substring(0, lemma.length() - 1) + "io";
                    } else if (lemma.equals("jis")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "o";
                    } else if (lemma.endsWith("is")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "io";
                    } else if (lemma.equals("pats")) {
                        pn = lemma.substring(0, lemma.length() - 1) + "ies";
                    }
                } else if (rqrd.equals("N.")) {
                    if (par1.contains(lemma)) {
                        pn = lemma.substring(0, lemma.length() - 2) + "am";
                    } else if (par2.contains(lemma)) {
                        pn = lemma.substring(0, lemma.length() - 2) + "am";
                    } else if (lemma.endsWith("oks")) {
                        pn = lemma.substring(0, lemma.length() - 1) + "iam";
                    } else if (lemma.equals("jis")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "am";
                    } else if (lemma.endsWith("is")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "iam";
                    } else if (lemma.equals("pats")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "čiam";
                    }
                } else if (rqrd.equals("G.")) {
                    if (par1.contains(lemma)) {
                        pn = lemma.substring(0, lemma.length() - 2) + "ą";
                    } else if (par2.contains(lemma)) {
                        pn = lemma.substring(0, lemma.length() - 2) + "ą";
                    } else if (lemma.endsWith("oks")) {
                        pn = lemma.substring(0, lemma.length() - 1) + "į";
                    } else if (lemma.equals("jis")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "į";
                    } else if (lemma.endsWith("is")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "į";
                    } else if (lemma.equals("pats")) {
                        pn = lemma.substring(0, lemma.length() - 1) + "į";
                    }
                } else if (rqrd.equals("Į.")) {
                    if (par1.contains(lemma)) {
                        pn = lemma.substring(0, lemma.length() - 2) + "uo";
                    } else if (par2.contains(lemma)) {
                        pn = lemma.substring(0, lemma.length() - 2) + "u";
                    } else if (lemma.endsWith("oks")) {
                        pn = lemma.substring(0, lemma.length() - 1) + "iu";
                    } else if (lemma.equals("jis")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "uo";
                    } else if (lemma.endsWith("is")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "iuo";
                    } else if (lemma.equals("pats")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "čiu";
                    }
                } else if (rqrd.equals("Vt.")) {
                    if (par1.contains(lemma)) {
                        pn = lemma.substring(0, lemma.length() - 2) + "ame";
                    } else if (par2.contains(lemma)) {
                        pn = lemma.substring(0, lemma.length() - 2) + "ame";
                    } else if (lemma.endsWith("oks")) {
                        pn = lemma.substring(0, lemma.length() - 1) + "iame";
                    } else if (lemma.equals("jis")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "ame";
                    } else if (lemma.endsWith("is")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "iame";
                    } else if (lemma.equals("pats")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "čiame";
                    }
                }
            } else {
                if (rqrd.equals("V.")) {
                    if (lemma.endsWith("as")) {
                        pn = lemma.substring(0, lemma.length() - 1);
                    } else if (lemma.endsWith("oks")) {
                        pn = lemma.substring(0, lemma.length() - 1) + "ia";
                    } else if (lemma.equals("jis")) {
                        pn = lemma.substring(0, lemma.length() - 1);
                    } else if (lemma.equals("pats")) {
                        pn = lemma.substring(0, lemma.length() - 1) + "i";
                    } else if (lemma.endsWith("is")) {
                        pn = lemma.substring(0, lemma.length() - 1);
                    }
                } else if (rqrd.equals("K.")) {
                    if (lemma.endsWith("as")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "os";
                    } else if (lemma.endsWith("oks")) {
                        pn = lemma.substring(0, lemma.length() - 1) + "ios";
                    } else if (lemma.equals("jis")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "os";
                    } else if (lemma.equals("pats")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "čios";
                    } else if (lemma.endsWith("is")) {
                        pn = lemma.substring(0, lemma.length() - 1) + "os";
                    }
                } else if (rqrd.equals("N.")) {
                    if (lemma.endsWith("as")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "ai";
                    } else if (lemma.endsWith("oks")) {
                        pn = lemma.substring(0, lemma.length() - 1) + "iai";
                    } else if (lemma.equals("jis")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "ai";
                    } else if (lemma.equals("pats")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "čiai";
                    } else if (lemma.endsWith("is")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "iai";
                    }
                } else if (rqrd.equals("G.")) {
                    if (lemma.endsWith("as")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "ą";
                    } else if (lemma.endsWith("oks")) {
                        pn = lemma.substring(0, lemma.length() - 1) + "ią";
                    } else if (lemma.equals("jis")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "ą";
                    } else if (lemma.equals("pats")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "čią";
                    } else if (lemma.endsWith("is")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "ią";
                    }
                } else if (rqrd.equals("Į.")) {
                    if (lemma.endsWith("as")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "a";
                    } else if (lemma.endsWith("oks")) {
                        pn = lemma.substring(0, lemma.length() - 1) + "ia";
                    } else if (lemma.equals("jis")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "a";
                    } else if (lemma.equals("pats")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "čia";
                    } else if (lemma.endsWith("is")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "ia";
                    }
                } else if (rqrd.equals("Vt.")) {
                    if (lemma.endsWith("as")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "oje";
                    } else if (lemma.endsWith("oks")) {
                        pn = lemma.substring(0, lemma.length() - 1) + "ioje";
                    } else if (lemma.equals("jis")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "oje";
                    } else if (lemma.equals("pats")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "čioje";
                    } else if (lemma.endsWith("is")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "ioje";
                    }
                }
            }
        } else {
            if (p.getGender().equals("vyr.g.")) {
                if (rqrd.equals("V.")) {
                    if (par1.contains(lemma)) {
                        pn = lemma.substring(0, lemma.length() - 2) + "ie";
                    } else if (par2.contains(lemma)) {
                        pn = lemma.substring(0, lemma.length() - 2) + "i";
                    } else if (lemma.endsWith("oks")) {
                        pn = lemma.substring(0, lemma.length() - 1) + "ie";
                    } else if (lemma.equals("jis")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "ie";
                    } else if (lemma.endsWith("is")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "ie";
                    } else if (lemma.equals("pats")) {
                        pn = lemma.substring(0, lemma.length() - 1) + "tys";
                    }
                } else if (rqrd.equals("K.")) {
                    if (par1.contains(lemma)) {
                        pn = lemma.substring(0, lemma.length() - 2) + "ų";
                    } else if (par2.contains(lemma)) {
                        pn = lemma.substring(0, lemma.length() - 2) + "ų";
                    } else if (lemma.endsWith("oks")) {
                        pn = lemma.substring(0, lemma.length() - 1) + "ių";
                    } else if (lemma.equals("jis")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "ų";
                    } else if (lemma.endsWith("is")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "ių";
                    } else if (lemma.equals("pats")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "čių";
                    }
                } else if (rqrd.equals("N.")) {
                    if (par1.contains(lemma)) {
                        pn = lemma.substring(0, lemma.length() - 2) + "iems";
                    } else if (par2.contains(lemma)) {
                        pn = lemma.substring(0, lemma.length() - 2) + "iems";
                    } else if (lemma.endsWith("oks")) {
                        pn = lemma.substring(0, lemma.length() - 1) + "iems";
                    } else if (lemma.equals("jis")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "iems";
                    } else if (lemma.endsWith("is")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "iems";
                    } else if (lemma.equals("pats")) {
                        pn = lemma.substring(0, lemma.length() - 1) + "tiems";
                    }
                } else if (rqrd.equals("G.")) {
                    if (par1.contains(lemma)) {
                        pn = lemma.substring(0, lemma.length() - 2) + "uos";
                    } else if (par2.contains(lemma)) {
                        pn = lemma.substring(0, lemma.length() - 2) + "us";
                    } else if (lemma.endsWith("oks")) {
                        pn = lemma.substring(0, lemma.length() - 1) + "ius";
                    } else if (lemma.equals("jis")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "uos";
                    } else if (lemma.endsWith("is")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "iuos";
                    } else if (lemma.equals("pats")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "čius";
                    }
                } else if (rqrd.equals("Į.")) {
                    if (par1.contains(lemma)) {
                        pn = lemma.substring(0, lemma.length() - 2) + "ais";
                    } else if (par2.contains(lemma)) {
                        pn = lemma.substring(0, lemma.length() - 2) + "ais";
                    } else if (lemma.endsWith("oks")) {
                        pn = lemma.substring(0, lemma.length() - 1) + "iais";
                    } else if (lemma.equals("jis")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "ais";
                    } else if (lemma.endsWith("is")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "iais";
                    } else if (lemma.equals("pats")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "čiais";
                    }
                } else if (rqrd.equals("Vt.")) {
                    if (par1.contains(lemma)) {
                        pn = lemma.substring(0, lemma.length() - 2) + "uose";
                    } else if (par2.contains(lemma)) {
                        pn = lemma.substring(0, lemma.length() - 2) + "uose";
                    } else if (lemma.endsWith("oks")) {
                        pn = lemma.substring(0, lemma.length() - 1) + "iuose";
                    } else if (lemma.equals("jis")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "uose";
                    } else if (lemma.endsWith("is")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "iuose";
                    } else if (lemma.equals("pats")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "čiuose";
                    }
                }
            } else {
                if (rqrd.equals("V.")) {
                    if (lemma.endsWith("as")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "os";
                    } else if (lemma.endsWith("oks")) {
                        pn = lemma.substring(0, lemma.length() - 1) + "ios";
                    } else if (lemma.equals("jis")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "os";
                    } else if (lemma.equals("pats")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "čios";
                    } else if (lemma.endsWith("is")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "ios";
                    }
                } else if (rqrd.equals("K.")) {
                    if (lemma.endsWith("as")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "ų";
                    } else if (lemma.endsWith("oks")) {
                        pn = lemma.substring(0, lemma.length() - 1) + "ių";
                    } else if (lemma.equals("jis")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "ų";
                    } else if (lemma.equals("pats")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "čių";
                    } else if (lemma.endsWith("is")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "ių";
                    }
                } else if (rqrd.equals("N.")) {
                    if (lemma.endsWith("as")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "oms";
                    } else if (lemma.endsWith("oks")) {
                        pn = lemma.substring(0, lemma.length() - 1) + "ioms";
                    } else if (lemma.equals("jis")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "oms";
                    } else if (lemma.equals("pats")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "čioms";
                    } else if (lemma.endsWith("is")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "ioms";
                    }
                } else if (rqrd.equals("G.")) {
                    if (lemma.endsWith("as")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "as";
                    } else if (lemma.endsWith("oks")) {
                        pn = lemma.substring(0, lemma.length() - 1) + "ias";
                    } else if (lemma.equals("jis")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "as";
                    } else if (lemma.equals("pats")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "čias";
                    } else if (lemma.endsWith("is")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "ias";
                    }
                } else if (rqrd.equals("Į.")) {
                    if (lemma.endsWith("as")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "omis";
                    } else if (lemma.endsWith("oks")) {
                        pn = lemma.substring(0, lemma.length() - 1) + "iomis";
                    } else if (lemma.equals("jis")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "omis";
                    } else if (lemma.equals("pats")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "čiomis";
                    } else if (lemma.endsWith("is")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "iomis";
                    }
                } else if (rqrd.equals("Vt.")) {
                    if (lemma.endsWith("as")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "ose";
                    } else if (lemma.endsWith("oks")) {
                        pn = lemma.substring(0, lemma.length() - 1) + "iose";
                    } else if (lemma.equals("jis")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "ose";
                    } else if (lemma.equals("pats")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "čiose";
                    } else if (lemma.endsWith("is")) {
                        pn = lemma.substring(0, lemma.length() - 2) + "iose";
                    }
                }
            }
        }
        return pn;
    }

}


