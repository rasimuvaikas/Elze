/**
* Eliza
* Author: Charles Hayden
* http://www.chayden.net/eliza/Eliza.html
* Modified by Andres Colubri to use it as a Processing library
*/

import processing.core.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 *  Eliza main class.
 *  Stores the processed script.
 *  Does the input transformations.
 */
public class Eliza {
  public Eliza(PApplet parent) {
		this.parent = parent;
		
		readDefaultScript();
  }
	
	public void dispose() {
		// anything in here will be called automatically when 
		// the parent applet shuts down. for instance, this might
		// shut down a thread used by this library.
		// note that this currently has issues, see bug #183
		// http://dev.processing.org/bugs/show_bug.cgi?id=183
    } 

    public boolean finished() {
        return finished;
    }

    /**
     *  Process a line of script input.
     */
    public void collect(String s) {
        String lines[] = new String[4];

        if (EString.match(s, "*reasmb: *", lines)) {
            if (lastReasemb == null) {
                System.out.println("Error: no last reasemb");
                return;
            }
            lastReasemb.add(lines[1]);
        }
        else if (EString.match(s, "*decomp: *", lines)) {
            if (lastDecomp == null) {
                System.out.println("Error: no last decomp");
                return;
            }
            lastReasemb = new ReasembList();
            String temp = new String(lines[1]);
            if (EString.match(temp, "$ *", lines)) {
                lastDecomp.add(lines[0], true, lastReasemb);
            } else {
                lastDecomp.add(temp, false, lastReasemb);
            }
        }
        else if (EString.match(s, "*key: * #*", lines)) {
            lastDecomp = new DecompList();
            lastReasemb = null;
            int n = 0;
            if (lines[2].length() != 0) {
                try {
                    n = Integer.parseInt(lines[2]);
                } catch (NumberFormatException e) {
                    System.out.println("Number is wrong in key: " + lines[2]);
                }
            }
            keys.add(lines[1], n, lastDecomp);
        }
        else if (EString.match(s, "*key: *", lines)) {
            lastDecomp = new DecompList();
            lastReasemb = null;
            keys.add(lines[1], 0, lastDecomp);
        }
        else if (EString.match(s, "*synon: * *", lines)) {
            WordList words = new WordList();
            words.add(lines[1]);
            s = lines[2];
            while (EString.match(s, "* *", lines)) {
                words.add(lines[0]);
                s = lines[1];
            }
            words.add(s);
            syns.add(words);
        }
        else if (EString.match(s, "*pre: * *", lines)) {
            pre.add(lines[1], lines[2]);
        }
        else if (EString.match(s, "*post: * *", lines)) {
            post.add(lines[1], lines[2]);
        }
        else if (EString.match(s, "*initial: *", lines)) {
            initial = lines[1];
        }
        else if (EString.match(s, "*final: *", lines)) {
            finl = lines[1];
        }
        else if (EString.match(s, "*quit: *", lines)) {
            quit.add(" " + lines[1]+ " ");
        }
        else {
            System.out.println("Unrecognized input: " + s);
        }
    }

    /**
     *  Print the stored script.
     */
    public void print() {
        if (printKeys) keys.print(0);
        if (printSyns) syns.print(0);
        if (printPrePost) {
            pre.print(0);
            post.print(0);
        }
        if (printInitialFinal) {
            System.out.println("initial: " + initial);
            System.out.println("final:   " + finl);
            quit.print(0);
            quit.print(0);
        }
    }

    /**
     *  Process a line of input.
     */
    public String processInput(String s) {
        String reply;
        //  Do some input transformations first.
        s = EString.translate(s, "ABCDEFGHIJKLMNOPQRSTUVWXYZ",
                                 "abcdefghijklmnopqrstuvwxyz");
        s = EString.translate(s, "@#$%^&*()_-+=~`{[}]|:;<>\\\"",
                                 "                          "  );
        s = EString.translate(s, ".?!", "..."); // pakečiau src: , į  src: ., žiūrėsim ar į gerą...
        //  Compress out multiple speace.
        s = EString.compress(s);
        String lines[] = new String[2];
        //  Break apart sentences, and do each separately.
        while (EString.match(s, "*.*", lines)) {
            reply = sentence(lines[0]);
            if (reply != null) return reply;
            s = EString.trim(lines[1]);
        }
        if (s.length() != 0) {
            reply = sentence(s);
            if (reply != null) return reply;
        }
        //  Nothing matched, so try memory.
        String m = mem.get();
        if (m != null) return m;

        //  No memory, reply with xnone.
        Key key = keys.getKey("xnone");
        if (key != null) {
            Key dummy = null;
            reply = decompose(key, s, dummy);
            if (reply != null) return reply;
        }
        //  No xnone, just say anything.
        return "Nežinau ką pasakyti.";
    }

	public boolean readDefaultScript()	{
		clearScript();
		
		/*
    // Returns the URL of the resource file inside the location in the jar
    // where the class file for Eliza is stored. More info about this here:
    // http://www.javaworld.com/javaworld/javaqa/2002-11/02-qa-1122-resources.html 		
		if (url != null)
		{
			String[] lines = parent.loadStrings(url.toString());
			for (int i = 0; i < lines.length; i++) {
				collect(lines[i]);
			}			
		}
		else System.err.println("Cannot load default Eliza script!");
		*/
		
		/*
		String[] lines = parent.loadStrings("eliza.script");
		if (lines.length == 0) {
		  System.err.println("Cannot load default Eliza script!");
		  return false;
		} else {
	    for (int i = 0; i < lines.length; i++) {
	      collect(lines[i]);
	    }
	    return true;
		}
		*/

		return readScript("scenarijus.script");
	}	
    
	public boolean readScript(String script) {
		clearScript();

		InputStream iS = getClass().getResourceAsStream(script);
    String[] lines = parent.loadStrings(iS);
    if (lines == null || lines.length == 0) {
      System.err.println("Cannot load Eliza script!");
      return false;
    } else {
      for (int i = 0; i < lines.length; i++) {
        collect(lines[i]);
      }
      return true;
    }

    
		/*
		InputStream stream = parent.openStream(script);
		if (stream == null) {
		    System.err.println("The script \"" + script + "\" " +
		                       "is missing or inaccessible, make sure " +
		                       "the URL is valid or that the file has been " +
		                       "added to your sketch and is readable.");
			return 1;
		}
		
		BufferedReader in = new BufferedReader(new InputStreamReader(stream));
			
		String s;	
	    while (true) {
			try {
				s = in.readLine();
	        } catch (IOException e) {
				System.err.println("Could not read line from script file.");
	            return 1;
	        }
	        if (s == null) break;
			collect(s);
	        if (echoInput) System.out.println(s);
	    }		
		
        if (printData) print();
        return 0;
        */
  }	
	
	void clearScript() {
		keys.clear();
		syns.clear();
		pre.clear();
		post.clear();
		initial = "";
		finl = "";
		quit.clear();
		keyStack.reset();
	}
	
    /**
     *  Process a sentence.
     *  (1) Make pre transformations.
     *  (2) Check for quit word.
     *  (3) Scan sentence for keys, build key stack.
     *  (4) Try decompositions for each key.
     */
    String sentence(String s) {

        /**
         * Handle the cases of pronoun omission:
         * insert 1st and 2nd person singular pronouns in front of 1st and 2nd person singular verbs if such pronouns
         * cannot be found in the sentence
         */
        try{
            Conjugator c = new Conjugator(s);
            Verb v;
            if(!s.contains("aš") && (((v = c.findVerb()) != null) && v.getPerson().equals("1asm.")))
            {
                String result = "";
                ArrayList<String> s1 = new ArrayList<>();
                String[] temp = s.split("\\s+");
                for (int j = 0; j < temp.length; j++) {

                    if (temp[j].equals(v.getVerb())) {
                        s1.add("aš");
                        s1.add(temp[j]);
                    }
                    else {
                        s1.add(temp[j]);
                    }
                }

                for(String p : s1)
                {
                    if(p.equals(s1.get(s1.size() - 1)))
                    {
                        result = result + p;
                    }
                    else {
                        result = result + p + " ";
                    }
                }

                s = result;
                System.out.println(result);
            }

            else if(!s.contains("tu") && (((v = c.findVerb()) != null) && v.getPerson().equals("2asm.")))
            {

                String result = "";
                ArrayList<String> s1 = new ArrayList<>();
                String[] temp = s.split("\\s+");
                for (int j = 0; j < temp.length; j++) {

                    if (temp[j].equals(v.getVerb())) {
                        s1.add("tu");
                        s1.add(temp[j]);
                    }
                    else {
                        s1.add(temp[j]);
                    }
                }

                for(String p : s1)
                {
                    if(p.equals(s1.get(s1.size() - 1)))
                    {
                        result = result + p;
                    }

                    else {
                        result = result + p + " ";
                    }
                }

                s = result;
            }
        }
        catch(IOException | URISyntaxException e)
        {
            e.getMessage();
        }
        s = pre.translate(s);
        s = EString.pad(s);
        if (quit.find(s)) {
            finished = true;
            return finl;
        }
        keys.buildKeyStack(keyStack, s);
        for (int i = 0; i < keyStack.keyTop(); i++) {
            Key gotoKey = new Key();
            String reply = decompose(keyStack.key(i), s, gotoKey);
            if (reply != null) return reply;
            //  If decomposition returned gotoKey, try it
            while (gotoKey.key() != null) {
                reply = decompose(gotoKey, s, gotoKey);
                if (reply != null) return reply;
            }
        }
        return null;
    }

    /**
     *  Decompose a string according to the given key.
     *  Try each decomposition rule in order.
     *  If it matches, assemble a reply and return it.
     *  If assembly fails, try another decomposition rule.
     *  If assembly is a goto rule, return null and give the key.
     *  If assembly succeeds, return the reply;
     */
    String decompose(Key key, String s, Key gotoKey) { //s yra input sentence

        String reply[] = new String[10];

        for (int i = 0; i < key.decomp().size(); i++) {
            Decomp d = (Decomp)key.decomp().elementAt(i);//gaunam pirmą decomp iš decomplist
            String pat = d.pattern(); //the current decomp pattern

            if (syns.matchDecomp(s, pat, reply)) {

                /**
                 * Conjugate or decline parts of speech if required by reassembly rules that belong to
                 * a specific decomp pattern.
                 * Different instance variables of type int are used to iterate through the list of
                 * reasmb rules and apply the varying necessary changes for each of the rules.
                 */
                if(pat.equals("* tu* mane *") || pat.equals("* tu *") || pat.equals("* tu mane *")
                || pat.equals("* tu manęs *") || pat.equals("* tu* manęs *"))
                {
                    try {
                        Conjugator conj = new Conjugator(reply[1]);
                        Verb verb;
                        if ((verb = conj.findVerb()) != null && verb.getPerson().equals("2asm."))
                        {
                            String result = "";
                            String[] temp = reply[1].split("\\s+");
                            for (int j = 0; j < temp.length; j++) {

                                if (temp[j].equals(verb.getVerb())) {
                                    temp[j] = conj.conjugateOppo(verb); //replace the original verb by a conjugated one
                                }

                                if (j == temp.length - 1) {
                                    result = result + temp[j];
                                } else {
                                    result = result + temp[j] + " ";
                                }
                            }
                            reply[1] = result;
                        }
                    }

                    catch(IOException | URISyntaxException e)
                    {
                        e.getMessage();
                    }
                }

                if(pat.equals("* aš @tikiu *aš *"))
                {
                    try {
                        Conjugator conj = new Conjugator(reply[3]);
                        Verb verb;
                        if ((verb = conj.findVerb()) != null && verb.getPerson().equals("1asm.")) //find vksm., 1. asm
                        {
                            String result = "";
                            String[] temp = reply[3].split("\\s+");
                            for (int j = 0; j < temp.length; j++) {

                                if (temp[j].equals(verb.getVerb())) {
                                    temp[j] = conj.conjugateOppo(verb);
                                }

                                if (j == temp.length - 1) {
                                    result = result + temp[j];
                                } else {
                                    result = result + temp[j] + " ";
                                }
                            }
                            reply[3] = result;
                        }
                    }
                    catch(IOException | URISyntaxException e)
                    {
                        e.getMessage();
                    }
                }

                if(pat.equals("* aš * tave *"))
                {

                    try {
                        Conjugator conj = new Conjugator(reply[1]);
                        Verb verb;

                        if(count == 0 && ((verb = conj.findVerb()) != null) && verb.getPerson().equals("1asm."))
                        {
                            String result = "";
                            String[] temp = reply[1].split("\\s+");
                            for (int j = 0; j < temp.length; j++) {

                                if (temp[j].equals(verb.getVerb())) {
                                    temp[j] = conj.conjugatePl(verb);
                                }

                                if (j == temp.length - 1) {
                                    result = result + temp[j];
                                } else {
                                    result = result + temp[j] + " ";
                                }
                            }

                            reply[1] = result;
                            ++count;
                        }


                        else if((count <= 2) && ((verb = conj.findVerb()) != null) && verb.getPerson().equals("1asm."))
                        {
                            String result = "";
                            String[] temp = reply[1].split("\\s+");
                            for (int j = 0; j < temp.length; j++) {

                                if (temp[j].equals(verb.getVerb())) {
                                    temp[j] = verb.getInf();
                                }

                                if (j == temp.length - 1) {
                                    result = result + temp[j];
                                } else {
                                    result = result + temp[j] + " ";
                                }
                            }

                            reply[1] = result;
                            ++count;
                        }

                        else if (count == 3 && ((verb = conj.findVerb()) != null) && verb.getPerson().equals("1asm.")) //find vksm., 1. asm
                        {
                            String result = "";
                            String[] temp = reply[1].split("\\s+");
                            for (int j = 0; j < temp.length; j++) {

                                if (temp[j].equals(verb.getVerb())) {
                                    temp[j] = conj.conjugateOppo(verb);
                                }

                                if (j == temp.length - 1) {
                                    result = result + temp[j];
                                } else {
                                    result = result + temp[j] + " ";
                                }
                            }

                            reply[1] = result;
                            count = 0;
                        }
                    }
                    catch(IOException | URISyntaxException e)
                    {
                        e.getMessage();
                    }
                }

                if(pat.equals("* aš tave *"))
                {

                    try {
                        Conjugator conj = new Conjugator(reply[1]);
                        Verb verb;

                        if(a == 0 && ((verb = conj.findVerb()) != null) && verb.getPerson().equals("1asm."))
                        {
                            String result = "";
                            String[] temp = reply[1].split("\\s+");
                            for (int j = 0; j < temp.length; j++) {

                                if (temp[j].equals(verb.getVerb())) {
                                    temp[j] = conj.conjugatePl(verb);
                                }

                                if (j == temp.length - 1) {
                                    result = result + temp[j];
                                } else {
                                    result = result + temp[j] + " ";
                                }
                            }

                            reply[1] = result;
                            ++a;
                        }


                        else if((a <= 2) && ((verb = conj.findVerb()) != null) && verb.getPerson().equals("1asm."))
                        {
                            String result = "";
                            String[] temp = reply[1].split("\\s+");
                            for (int j = 0; j < temp.length; j++) {

                                if (temp[j].equals(verb.getVerb())) {
                                    temp[j] = verb.getInf();
                                }

                                if (j == temp.length - 1) {
                                    result = result + temp[j];
                                } else {
                                    result = result + temp[j] + " ";
                                }
                            }

                            reply[1] = result;
                            ++a;
                        }

                        else if (a == 3 && ((verb = conj.findVerb()) != null) && verb.getPerson().equals("1asm.")) //find vksm., 1. asm
                        {
                            String result = "";
                            String[] temp = reply[1].split("\\s+");
                            for (int j = 0; j < temp.length; j++) {

                                if (temp[j].equals(verb.getVerb())) {
                                    temp[j] = conj.conjugateOppo(verb);
                                }

                                if (j == temp.length - 1) {
                                    result = result + temp[j];
                                } else {
                                    result = result + temp[j] + " ";
                                }
                            }

                            reply[1] = result;
                            a = 0;
                        }
                    }
                    catch(IOException | URISyntaxException e)
                    {
                        e.getMessage();
                    }
                }

                if(pat.equals("* aš tavęs *"))
                {

                    try {
                        Conjugator conj = new Conjugator(reply[1]);
                        Verb verb;

                        if(b == 0 && ((verb = conj.findVerb()) != null) && verb.getPerson().equals("1asm."))
                        {
                            String result = "";
                            String[] temp = reply[1].split("\\s+");
                            for (int j = 0; j < temp.length; j++) {

                                if (temp[j].equals(verb.getVerb())) {
                                    temp[j] = conj.conjugatePl(verb);
                                }

                                if (j == temp.length - 1) {
                                    result = result + temp[j];
                                } else {
                                    result = result + temp[j] + " ";
                                }
                            }

                            reply[1] = result;
                            ++b;
                        }


                        else if((b <= 2) && ((verb = conj.findVerb()) != null) && verb.getPerson().equals("1asm."))
                        {
                            String result = "";
                            String[] temp = reply[1].split("\\s+");
                            for (int j = 0; j < temp.length; j++) {

                                if (temp[j].equals(verb.getVerb())) {
                                    temp[j] = verb.getInf();
                                }

                                if (j == temp.length - 1) {
                                    result = result + temp[j];
                                } else {
                                    result = result + temp[j] + " ";
                                }
                            }

                            reply[1] = result;
                            ++b;
                        }

                        else if (b == 3 && ((verb = conj.findVerb()) != null) && verb.getPerson().equals("1asm.")) //find vksm., 1. asm
                        {
                            String result = "";
                            String[] temp = reply[1].split("\\s+");
                            for (int j = 0; j < temp.length; j++) {

                                if (temp[j].equals(verb.getVerb())) {
                                    temp[j] = conj.conjugateOppo(verb);
                                }

                                if (j == temp.length - 1) {
                                    result = result + temp[j];
                                } else {
                                    result = result + temp[j] + " ";
                                }
                            }

                            reply[1] = result;
                            b = 0;
                        }
                    }
                    catch(IOException | URISyntaxException e)
                    {
                        e.getMessage();
                    }
                }

                if(pat.equals("* aš * tavęs *"))
                {

                    try {
                        Conjugator conj = new Conjugator(reply[1]);
                        Verb verb;

                        if(c == 0 && ((verb = conj.findVerb()) != null) && verb.getPerson().equals("1asm."))
                        {
                            String result = "";
                            String[] temp = reply[1].split("\\s+");
                            for (int j = 0; j < temp.length; j++) {

                                if (temp[j].equals(verb.getVerb())) {
                                    temp[j] = conj.conjugatePl(verb);
                                }

                                if (j == temp.length - 1) {
                                    result = result + temp[j];
                                } else {
                                    result = result + temp[j] + " ";
                                }
                            }

                            reply[1] = result;
                            ++c;
                        }


                        else if((c <= 2) && ((verb = conj.findVerb()) != null) && verb.getPerson().equals("1asm."))
                        {
                            String result = "";
                            String[] temp = reply[1].split("\\s+");
                            for (int j = 0; j < temp.length; j++) {

                                if (temp[j].equals(verb.getVerb())) {
                                    temp[j] = verb.getInf();
                                }

                                if (j == temp.length - 1) {
                                    result = result + temp[j];
                                } else {
                                    result = result + temp[j] + " ";
                                }
                            }

                            reply[1] = result;
                            ++c;
                        }

                        else if (c == 3 && ((verb = conj.findVerb()) != null) && verb.getPerson().equals("1asm.")) //find vksm., 1. asm
                        {
                            String result = "";
                            String[] temp = reply[1].split("\\s+");
                            for (int j = 0; j < temp.length; j++) {

                                if (temp[j].equals(verb.getVerb())) {
                                    temp[j] = conj.conjugateOppo(verb);
                                }

                                if (j == temp.length - 1) {
                                    result = result + temp[j];
                                } else {
                                    result = result + temp[j] + " ";
                                }
                            }

                            reply[1] = result;
                            c = 0;
                        }
                    }
                    catch(IOException | URISyntaxException e)
                    {
                        e.getMessage();
                    }
                }

                if(pat.equals("* aš *"))
                {
                    try {
                        Conjugator conj = new Conjugator(reply[1]);
                        Verb verb;
                        if ((rcount <=2 || rcount == 4) && ((verb = conj.findVerb()) != null) && verb.getPerson().equals("1asm.")) //find vksm., 2. asm
                        {
                            String result = "";
                            String[] temp = reply[1].split("\\s+");
                            for (int j = 0; j < temp.length; j++) {

                                if (temp[j].equals(verb.getVerb())) {
                                    temp[j] = conj.conjugateOppo(verb);
                                }

                                if (j == temp.length - 1) {
                                    result = result + temp[j];
                                } else {
                                    result = result + temp[j] + " ";
                                }
                            }

                            reply[1] = result;
                            ++rcount;
                        }

                        else
                        {
                            if(rcount == 6) {
                                rcount = 0;
                            }

                            else
                            {
                                ++rcount;
                            }
                        }
                    }
                    catch(IOException | URISyntaxException e)
                    {
                        e.getMessage();
                    }
                }



                if(pat.equals("* kodėl tu *")) {
                    try {
                        Conjugator conj = new Conjugator(reply[1]);
                        Verb verb;
                        if (dcount == 0 && ((verb = conj.findVerb()) != null) && verb.getPerson().equals("2asm.")) //find vksm., 2. asm
                        {
                            String result = "";
                            String[] temp = reply[1].split("\\s+");
                            for (int j = 0; j < temp.length; j++) {

                                if (temp[j].equals(verb.getVerb())) {
                                    temp[j] = conj.conjugateOppo(verb);
                                }

                                if (j == temp.length - 1) {
                                    result = result + temp[j];
                                } else {
                                    result = result + temp[j] + " ";
                                }
                            }

                            reply[1] = result;
                            ++dcount;
                        }

                        else if (dcount == 2 && ((verb = conj.findVerb()) != null) && verb.getPerson().equals("2asm.")) {
                            String result = "";
                            String[] temp = reply[1].split("\\s+");
                            for (int j = 0; j < temp.length; j++) {

                                if (temp[j].equals(verb.getVerb())) {
                                    temp[j] = verb.getInf();
                                }

                                if (j == temp.length - 1) {
                                    result = result + temp[j];
                                } else {
                                    result = result + temp[j] + " ";
                                }
                            }
                            if (result.contains("manęs")) {
                                result = result.replace("manęs", "savęs");
                            }
                            reply[1] = result;
                            ++dcount;
                        }

                        else if (dcount == 3 && ((verb = conj.findVerb()) != null) && verb.getPerson().equals("2asm.")) {
                            String result = "";
                            String[] temp = reply[1].split("\\s+");
                            for (int j = 0; j < temp.length; j++) {

                                if (temp[j].equals(verb.getVerb())) {
                                    temp[j] = conj.conjugateSubj(verb);
                                }

                                if (j == temp.length - 1) {
                                    result = result + temp[j];
                                } else {
                                    result = result + temp[j] + " ";
                                }
                            }

                            reply[1] = result;
                            ++dcount;
                        }
                        else {
                            if (dcount == 4) {
                                dcount = 0;
                            } else {
                                ++dcount;
                            }
                        }
                    } catch (IOException | URISyntaxException e) {
                        e.getMessage();
                    }
                }

                if(pat.equals("* tavo *"))
                {
                    try {
                        Conjugator conj = new Conjugator(reply[1]);
                        Noun noun = null;
                        Adjective adj = null;
                        Pronoun p = null;

                        if(ncount == 0 && ((noun = conj.findNoun()) != null))
                        {
                            String result = "";
                            String[] temp = reply[1].split("\\s+");
                                for (int j = 0; j < temp.length; j++) {

                                    if (temp[j].equals(noun.getNoun())) {
                                        temp[j] = conj.declineN(noun, "V.");
                                    }
                                    if (j == temp.length - 1) {
                                        result = result + temp[j];
                                    } else {
                                        result = result + temp[j] + " ";
                                    }
                                }

                                if((adj = conj.findAdjective()) != null)
                                {
                                    String fresult = "";
                                    String[] t = result.split("\\s+");

                                    for (int j = 0; j < t.length; j++) {

                                        if (t[j].equals(adj.getAdj())){
                                            t[j] = conj.declineA(adj, "V.");
                                        }
                                        if (j == t.length - 1) {
                                            fresult = fresult + t[j];
                                        } else {
                                            fresult = fresult + t[j] + " ";
                                        }
                                    }
                                    result = fresult;
                                }

                                if(((p = conj.findPronoun()) != null) && p.getDecl().equals(noun.getDecl()))
                                {
                                    String presult = "";
                                    String[] t = result.split("\\s+");

                                    for (int j = 0; j < t.length; j++) {

                                        if (t[j].equals(p.getPronoun())){
                                            t[j] = conj.declineP(p, "V.");
                                        }
                                        if (j == t.length - 1) {
                                            presult = presult + t[j];
                                        } else {
                                            presult = presult + t[j] + " ";
                                        }
                                    }
                                    result = presult;
                                }
                                reply[1] = result;
                                ncount++;
                            }
                        else if((ncount == 1 || ncount == 2) && (noun = conj.findNoun()) != null)
                        {
                            String result = "";
                            String[] temp = reply[1].split("\\s+");
                            for (int j = 0; j < temp.length; j++) {

                                if (temp[j].equals(noun.getNoun())) {
                                    temp[j] = conj.declineN(noun, "K.");
                                }
                                if (j == temp.length - 1) {
                                    result = result + temp[j];
                                } else {
                                    result = result + temp[j] + " ";
                                }
                            }

                            if((adj = conj.findAdjective()) != null)
                            {
                                String fresult = "";
                                String[] t = result.split("\\s+");

                                for (int j = 0; j < t.length; j++) {

                                    if (t[j].equals(adj.getAdj())){
                                        t[j] = conj.declineA(adj, "K.");
                                    }
                                    if (j == t.length - 1) {
                                        fresult = fresult + t[j];
                                    } else {
                                        fresult = fresult + t[j] + " ";
                                    }
                                }
                                result = fresult;
                            }

                            if(((p = conj.findPronoun()) != null) && p.getDecl().equals(noun.getDecl()))
                            {
                                String presult = "";
                                String[] t = result.split("\\s+");

                                for (int j = 0; j < t.length; j++) {

                                    if (t[j].equals(p.getPronoun())){
                                        t[j] = conj.declineP(p, "K.");
                                    }
                                    if (j == t.length - 1) {
                                        presult = presult + t[j];
                                    } else {
                                        presult = presult + t[j] + " ";
                                    }
                                }
                                result = presult;
                            }
                            reply[1] = result;
                            ncount++;
                        } else {
                            ncount = 0;
                        }
                    }

                    catch(IOException | URISyntaxException e)
                    {
                        e.getMessage();
                    }
                }

                if(pat.equals("* aš @trokštu *"))
                {
                    try {
                        Conjugator conj = new Conjugator(reply[2]);
                        Noun noun = null;
                        Adjective adj = null;
                        Pronoun p = null;

                        if((tcount == 0 || tcount == 2) && ((noun = conj.findNoun()) != null)
                                && noun.getDecl().equals("K."))
                        {
                            String result = "";
                            String[] temp = reply[2].split("\\s+");

                            for (int j = 0; j < temp.length; j++) {

                                if (temp[j].equals(noun.getNoun())) {
                                    temp[j] = conj.declineN(noun, "G.");
                                }
                                if (j == temp.length - 1) {
                                    result = result + temp[j];
                                } else {
                                    result = result + temp[j] + " ";
                                }
                            }

                            if(((adj = conj.findAdjective()) != null) && adj.getDecl().equals(noun.getDecl()))
                            {
                                String fresult = "";
                                String[] t = result.split("\\s+");

                                for (int j = 0; j < t.length; j++) {

                                    if (t[j].equals(adj.getAdj())){
                                        t[j] = conj.declineA(adj, "G.");
                                    }
                                    if (j == t.length - 1) {
                                        fresult = fresult + t[j];
                                    } else {
                                        fresult = fresult + t[j] + " ";
                                    }
                                }
                                result = fresult;
                            }
                            reply[2] = result;
                            tcount++;
                        }
                        else if(tcount == 4 && ((noun = conj.findNoun()) != null) && noun.getDecl().equals("K."))
                        {
                            String result = "";
                            String[] temp = reply[2].split("\\s+");
                            for (int j = 0; j < temp.length; j++) {

                                if (temp[j].equals(noun.getNoun())) {
                                    temp[j] = conj.declineN(noun, "V.");
                                }
                                if (j == temp.length - 1) {
                                    result = result + temp[j];
                                } else {
                                    result = result + temp[j] + " ";
                                }
                            }

                            if(((adj = conj.findAdjective()) != null) && p.getDecl().equals(noun.getDecl()))
                            {
                                String fresult = "";
                                String[] t = result.split("\\s+");

                                for (int j = 0; j < t.length; j++) {

                                    if (t[j].equals(adj.getAdj())){
                                        t[j] = conj.declineA(adj, "V.");
                                    }
                                    if (j == t.length - 1) {
                                        fresult = fresult + t[j];
                                    } else {
                                        fresult = fresult + t[j] + " ";
                                    }
                                }
                                result = fresult;
                            }

                            if(((p = conj.findPronoun()) != null) && p.getDecl().equals(noun.getDecl()))
                            {
                                String presult = "";
                                String[] t = result.split("\\s+");

                                for (int j = 0; j < t.length; j++) {

                                    if (t[j].equals(p.getPronoun())){
                                        t[j] = conj.declineP(p, "V.");
                                    }
                                    if (j == t.length - 1) {
                                        presult = presult + t[j];
                                    } else {
                                        presult = presult + t[j] + " ";
                                    }
                                }
                                result = presult;
                            }
                            reply[1] = result;
                            tcount++;
                        } else {
                            if(tcount == 5) {
                                tcount = 0;
                            } else{
                                tcount++;
                            }
                        }
                    }

                    catch(IOException | URISyntaxException e)
                    {
                        e.getMessage();
                    }
                }

                if(pat.equals("* man *reikia *"))
                {
                    try {
                        Conjugator conj = new Conjugator(reply[2]);
                        Noun noun = null;
                        Adjective adj = null;
                        Pronoun p = null;

                        if((mcount == 0 || mcount == 2) && ((noun = conj.findNoun()) != null)
                                && noun.getDecl().equals("K."))
                        {
                            String result = "";
                            String[] temp = reply[2].split("\\s+");

                            for (int j = 0; j < temp.length; j++) {

                                if (temp[j].equals(noun.getNoun())) {
                                    temp[j] = conj.declineN(noun, "G.");
                                }
                                if (j == temp.length - 1) {
                                    result = result + temp[j];
                                } else {
                                    result = result + temp[j] + " ";
                                }
                            }

                            if(((adj = conj.findAdjective()) != null) && adj.getDecl().equals(noun.getDecl()))
                            {
                                String fresult = "";
                                String[] t = result.split("\\s+");

                                for (int j = 0; j < t.length; j++) {

                                    if (t[j].equals(adj.getAdj())){
                                        t[j] = conj.declineA(adj, "G.");
                                    }
                                    if (j == t.length - 1) {
                                        fresult = fresult + t[j];
                                    } else {
                                        fresult = fresult + t[j] + " ";
                                    }
                                }
                                result = fresult;
                            }
                            reply[2] = result;
                            mcount++;
                        }
                        else if(mcount == 4 && ((noun = conj.findNoun()) != null) && noun.getDecl().equals("K."))
                        {
                            String result = "";
                            String[] temp = reply[2].split("\\s+");
                            for (int j = 0; j < temp.length; j++) {

                                if (temp[j].equals(noun.getNoun())) {
                                    temp[j] = conj.declineN(noun, "V.");
                                }
                                if (j == temp.length - 1) {
                                    result = result + temp[j];
                                } else {
                                    result = result + temp[j] + " ";
                                }
                            }

                            if(((adj = conj.findAdjective()) != null) && p.getDecl().equals(noun.getDecl()))
                            {
                                String fresult = "";
                                String[] t = result.split("\\s+");

                                for (int j = 0; j < t.length; j++) {

                                    if (t[j].equals(adj.getAdj())){
                                        t[j] = conj.declineA(adj, "V.");
                                    }
                                    if (j == t.length - 1) {
                                        fresult = fresult + t[j];
                                    } else {
                                        fresult = fresult + t[j] + " ";
                                    }
                                }
                                result = fresult;
                            }

                            if(((p = conj.findPronoun()) != null) && p.getDecl().equals(noun.getDecl()))
                            {
                                String presult = "";
                                String[] t = result.split("\\s+");

                                for (int j = 0; j < t.length; j++) {

                                    if (t[j].equals(p.getPronoun())){
                                        t[j] = conj.declineP(p, "V.");
                                    }
                                    if (j == t.length - 1) {
                                        presult = presult + t[j];
                                    } else {
                                        presult = presult + t[j] + " ";
                                    }
                                }
                                result = presult;
                            }
                            reply[1] = result;
                            mcount++;
                        } else {
                            if(mcount == 5) {
                                mcount = 0;
                            } else{
                                mcount++;
                            }
                        }
                    }

                    catch(IOException | URISyntaxException e)
                    {
                        e.getMessage();
                    }
                }
                    if(pat.equals("* aš * @liūdnas *"))
                    {
                        try {
                            Conjugator conj = new Conjugator(reply[2]);
                            Adjective adj = conj.findAdjective();
                            if (bcount == 1 || bcount == 2)
                            {
                                String result = conj.declineA(adj, "N.");
                                reply[2] = result;
                                bcount++;
                            }
                            else if(bcount == 3)
                            {
                                String result = conj.declineA(adj, "G.");
                                reply[2] = result;
                                bcount = 0;
                            }

                            else
                            {
                                bcount++;
                            }
                        }

                        catch(IOException | URISyntaxException e)
                        {
                            e.getMessage();
                        }
                    }

                if(pat.equals("* aš * @laimingas *"))
                {
                    try {
                        Conjugator conj = new Conjugator(reply[2]);
                        Adjective adj = conj.findAdjective();
                        if (ccount == 1 || ccount == 2)
                        {
                            String result = conj.declineA(adj, "G.");
                            reply[2] = result;
                            ccount++;
                        }
                        else if(ccount == 0)
                        {
                            String result = conj.declineA(adj, "Į.");
                            reply[2] = result;
                            ccount++;
                        }

                        else
                        {
                            ccount = 0;
                        }
                    }

                    catch(IOException | URISyntaxException e)
                    {
                        e.getMessage();
                    }
                }

                if(pat.equals("* aš esu *"))
                {
                    try {
                        Conjugator conj = new Conjugator(reply[1]);
                        Noun noun = null;
                        Adjective adj = null;
                        Pronoun p = null;

                        if(acount <=1){
                            acount++;
                        }
                        else if(acount == 2 || acount == 3) {
                            String result = "";
                            String[] temp = reply[1].split("\\s+");
                            if ((noun = conj.findNoun()) != null){
                                for (int j = 0; j < temp.length; j++) {

                                    if (temp[j].equals(noun.getNoun())) {
                                        temp[j] = conj.declineN(noun, "N.");
                                    }
                                    if (j == temp.length - 1) {
                                        result = result + temp[j];
                                    } else {
                                        result = result + temp[j] + " ";
                                    }
                                }
                        }

                            if((adj = conj.findAdjective()) != null)
                            {
                                for (int j = 0; j < temp.length; j++) {

                                    if (temp[j].equals(adj.getAdj())){
                                        temp[j] = conj.declineA(adj, "N.");
                                    }
                                    if (j == temp.length - 1) {
                                        result = result + temp[j];
                                    } else {
                                        result = result + temp[j] + " ";
                                    }
                                }
                            }

                            if(((p = conj.findPronoun()) != null) && p.getDecl().equals("V."))
                            {
                                for (int j = 0; j < temp.length; j++) {

                                    if (temp[j].equals(p.getPronoun())){
                                        temp[j] = conj.declineP(p, "N.");
                                    }
                                    if (j == temp.length - 1) {
                                        result = result + temp[j];
                                    } else {
                                        result = result + temp[j] + " ";
                                    }
                                }
                            }

                            if(acount == 3)
                            {
                                acount = 0;
                            }
                            else {
                                acount++;
                            }
                            reply[1] = result;
                        }
                    }

                    catch(IOException | URISyntaxException e)
                    {
                        e.getMessage();
                    }
                }

                if(pat.equals("* mano* @šeima *"))
                {
                    try {
                    Conjugator conj = new Conjugator(reply[2]);
                    Noun n = conj.findNoun();
                    if(fcount == 3)
                    {
                        String result = conj.declineN(n, "G.");
                        reply[2] = result;
                        fcount = 0;
                    }
                    else
                    {
                        fcount++;
                    }
                    }
                    catch(IOException | URISyntaxException e)
                    {
                    e.getMessage();
                    }
                }

                String rep = assemble(d, reply, gotoKey);
                if (rep != null) return rep;
                if (gotoKey.key() != null) return null;
            }
        }
        return null;
    }

    /**
     *  Assembly a reply from a decomp rule and the input.
     *  If the reassembly rule is goto, return null and give
     *    the gotoKey to use.
     *  Otherwise return the response.
     */
    String assemble(Decomp d, String reply[], Key gotoKey) {
        String lines[] = new String[3];
        d.stepRule();
        String rule = d.nextRule();

        if (EString.match(rule, "goto *", lines)) {
            //  goto rule -- set gotoKey and return false.
            gotoKey.copy(keys.getKey(lines[0]));
            if (gotoKey.key() != null) return null;
            System.out.println("Goto rule did not match key: " + lines[0]);
            return null;
        }
        String work = "";
        while (EString.match(rule, "* (#)*", lines)) {
            //  reassembly rule with number substitution
            rule = lines[2];        // there might be more
            int n = 0;
            try {
                n = Integer.parseInt(lines[1]) - 1;
            } catch (NumberFormatException e) {
                System.out.println("Number is wrong in reassembly rule " + lines[1]);
            }
            if (n < 0 || n >= reply.length) {
                System.out.println("Substitution number is bad " + lines[1]);
                return null;
            }
            reply[n] = post.translate(reply[n]);
            work += lines[0] + " " + reply[n];
        }
        work += rule;
        if (d.mem()) {
            mem.save(work);
            return null;
        }
        return work;
    }
    
    PApplet parent;
    
    final boolean echoInput = false;
    final boolean printData = false;

    final boolean printKeys = false;
    final boolean printSyns = false;
    final boolean printPrePost = false;
    final boolean printInitialFinal = false;

    /** The key list */
    KeyList keys = new KeyList();
    /** The syn list */
    SynList syns = new SynList();
    /** The pre list */
    PrePostList pre = new PrePostList();
    /** The post list */
    PrePostList post = new PrePostList();
    /** Initial string */
    String initial = "Labas.";
    /** Final string */
    String finl = "Iki.";
    /** Quit list */
    WordList quit = new WordList();

    /** Key stack */
    KeyStack keyStack = new KeyStack();

    /** Memory */
    Mem mem = new Mem();

    DecompList lastDecomp;
    ReasembList lastReasemb;
    boolean finished = false;

    static final int success = 0;
    static final int failure = 1;
    static final int gotoRule = 2;

    int rcount = 0;
    int dcount = 0;
    int bcount = 0;
    int ncount = 0;
    int tcount = 0;
    int ccount = 0;
    int acount = 0;
    int fcount = 0;
    int mcount = 0;
    int count = 0;
    int a = 0;
    int b = 0;
    int c = 0;
}
