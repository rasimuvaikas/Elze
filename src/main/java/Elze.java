//import codeanticode.eliza.Eliza;
import processing.core.PApplet;
import processing.core.PFont;

public class Elze extends PApplet
{
    /*String result="";
    Eliza el;
    TextField t = new TextField();
    Scanner s = new Scanner(System.in);*/


    public static void main(String[] args)
    {
        PApplet.main("Elze");
    }


    Eliza eliza;
    PFont font;
    String elizaResponse, humanResponse;
    boolean showCursor;
    int lastTime;

    public void setup()
    {


        // When Eliza is initialized, a default script built into the
        // library is loaded.
        eliza = new Eliza(this);

        // A new script can be loaded through the readScript function.
        // It can take local as well as remote files.
        //eliza.readScript("script");
        //eliza.readScript("http://chayden.net/eliza/script");

        // To go back to the default script, use this:
        //eliza.readDefaultScript();

        //font = loadFont("Rockwell-24.vlw");
        //textFont(font);

        printElizaIntro();
        humanResponse = "";
        showCursor = true;
        lastTime = 0;
    }

    public void settings()
    {
        size(400, 400);
    }

    public void draw()
    {
        background(102);

        fill(255);
        text(elizaResponse, 10, 50, width - 40, height);

        fill(0);

        int t = millis();
        if (t - lastTime > 500)
        {
            showCursor = !showCursor;
            lastTime = t;
        }

        if (showCursor) text(humanResponse + "_", 10, 150, width - 40, height);
        else text(humanResponse, 10, 150, width - 40, height);
    }

    public void keyPressed()
    {
        if ((key == ENTER) || (key == RETURN))
        {
            println(humanResponse);
            elizaResponse = eliza.processInput(humanResponse);
            println(">> " + elizaResponse);
            humanResponse = "";
        }
        else if ((key > 31) && (key != CODED))
        {
            // If the key is alphanumeric, add it to the String
            humanResponse = humanResponse + key;
        }
        else if ((key == BACKSPACE) && (0 < humanResponse.length()))
        {
            char c = humanResponse.charAt(humanResponse.length() - 1);
            humanResponse = humanResponse.substring(0, humanResponse.length() - 1);
        }
    }

    void printElizaIntro()
    {
        String hello = "Labas.";
        elizaResponse = hello + " " + eliza.processInput(hello);
        println(">> " + elizaResponse);
    }





}
