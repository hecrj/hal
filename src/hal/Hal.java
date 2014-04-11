package hal;

// Imports for ANTLR
import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;

// Imports from Java
import org.apache.commons.cli.*; // Command Language Interface
import java.io.*;
import java.util.NoSuchElementException;
import java.util.Scanner;

// Parser and Interpreter
import hal.parser.*;
import hal.interpreter.*;

/**
 * The class <code>Hal</code> implement the main function of the
 * interpreter. It accepts a set of options to generate the AST in
 * dot format and avoid the execution of the program. To know about
 * the accepted options, run the command hal -help.
 */

public class Hal
{
    public static final String VERSION = "0.0.1";
    public static final String DATE = "Apr 09, 18:47";

    private static Interpreter INTERPRETER = null;
    /** The file name of the program. */
    private static String infile = null;
    private static File astfile = null;
    /** Flag indicating that the AST must be written in dot format. */
    private static boolean dotformat = false;
    /** Name of the file storing the trace of the program. */
    private static PrintWriter tracefile = null;
    /** Flag to indicate whether the program must be executed after parsing. */
    private static boolean execute = true;
    /** Flag to indicate whether the interpreter works in interactive mode. */
    private static boolean interactive = false;
      
    /** Main program that invokes the parser and the interpreter. */
    
    public static void main(String[] args) throws Exception {
        try {
            // Parser for command line options
            if (!readOptions (args))
                System.exit(1);

            INTERPRETER = new Interpreter(tracefile); // prepares the interpreter

            if(interactive)
                interactiveMode();
            else
                fileMode();
        } catch (IOException e) {
            System.err.println ("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void interactiveMode() throws IOException {
        System.out.println("Hal " + VERSION + " (" + DATE + ") [java "
                + System.getProperty("java.version") + "]");

        Scanner keyboard = new Scanner(System.in);
        String input;
        boolean quit = false;
        while(!quit) {
            input = "";
            System.out.print("\n>>> ");
            try {
                input = keyboard.nextLine();
                if(input.endsWith(":")) {
                    String block;
                    do {
                        System.out.print("... ");
                        block = keyboard.nextLine();
                        input += block;
                    } while(!block.equals(""));
                }
            } catch(NoSuchElementException e) {
                if(input.isEmpty()) {
                    System.out.println();
                    break;
                } else {
                    quit = true;
                }
            }

            if(input.equals("quit"))
                quit = true;

            try {
                DataType d = process(new ANTLRStringStream(input));

                if (d != null)
                    System.out.print(d.__repr__());
            } catch(RuntimeException e) {
                System.out.println("Syntax error.");
            }
        }
    }

    private static void fileMode() throws IOException {
        CharStream input = null;

        try {
            input = new ANTLRFileStream(infile);
            process(input);
        } catch(RuntimeException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private static DataType process(CharStream source) throws IOException {
        HalTree tree = getTree(source);

        if(astfile != null)
            writeASTFile(tree);

        if(execute)
            return evaluate(tree);

        return null;
    }

    private static HalTree getTree(CharStream source) {
        // Creates the lexer
        HalLexer lex = new HalLexer(source);
        CommonTokenStream tokens = new CommonTokenStream(lex);

        // Creates and runs the parser. As a result, an AST is created
        HalParser parser = new HalParser(tokens);
        HalTreeAdaptor adaptor = new HalTreeAdaptor();
        parser.setTreeAdaptor(adaptor);
        HalParser.prog_return result = null;
        try {
            result = parser.prog();
        } catch (Exception e) {} // Just catch the exception (nothing to do)

        // Check for parsing errors
        int nerrors = parser.getNumberOfSyntaxErrors();
        if (nerrors > 0)
            throw new RuntimeException(nerrors + " errors detected. The program has not been executed.");

        return (HalTree)result.getTree();
    }

    private static void writeASTFile(HalTree t) throws IOException {
        BufferedWriter output = new BufferedWriter(new FileWriter(astfile, true));
        if (dotformat) {
            DOTTreeGenerator gen = new DOTTreeGenerator();
            output.write(gen.toDOT(t).toString());
        } else {
            output.write(t.toStringTree());
        }
        output.close();
    }

    private static DataType evaluate(HalTree t) {
        int linenumber = -1;
        try {
            return INTERPRETER.Run(t);                  // Executes the code
        } catch (RuntimeException e) {
            if (INTERPRETER != null) linenumber = INTERPRETER.lineNumber();
            System.err.print (e.getClass().getSimpleName());
            if (linenumber < 0) System.err.print (": ");
            else System.err.print (" (" + infile + ", line " + linenumber + "): ");
            System.err.println (e.getMessage() + ".");
            System.err.format(INTERPRETER.getStackTrace());
            if(e instanceof NullPointerException)
                e.printStackTrace(System.err);
        } catch (StackOverflowError e) {
            if (INTERPRETER != null) linenumber = INTERPRETER.lineNumber();
            System.err.print("Stack overflow error");
            if (linenumber < 0) System.err.print (".");
            else System.err.println (" (" + infile + ", line " + linenumber + ").");
            System.err.format (INTERPRETER.getStackTrace(5));
        }

        return null;
    }

    /**
     * Function to parse the command line. It defines some of
     * the attributes of the class. It returns true if the parsing
     * hass been successful, and false otherwise.
     */

    private static boolean readOptions(String[] args) throws IOException {
        // Define the options
        Option help = new Option("help", "print this message");
        Option noexec = new Option("noexec", "do not execute the program");
        Option dot = new Option("dot", "dump the AST in dot format");
        Option ast = OptionBuilder
                        .withArgName ("file")
                        .hasArg()
                        .withDescription ("write the AST")
                        .create ("ast");
        Option trace = OptionBuilder
                        .withArgName ("file")
                        .hasArg()
                        .withDescription ("write a trace of function calls during the execution of the program")
                        .create ("trace");
                                       
        Options options = new Options();
        options.addOption(help);
        options.addOption(dot);
        options.addOption(ast);
        options.addOption(trace);
        options.addOption(noexec);
        CommandLineParser clp = new GnuParser();
        CommandLine line = null;

        String cmdline = "hal [options] file";
        
        
        // Parse the options
        try {
            line = clp.parse (options, args);
        }
        catch (ParseException exp) {
            System.err.println ("Incorrect command line: " + exp.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp (cmdline, options);
            return false;
        }

        // Option -help
        if (line.hasOption ("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp (cmdline, options);
            return false;
        }
        
        // Option -dot
        if (line.hasOption ("dot")) dotformat = true;

        // Option -ast dotfile
        if (line.hasOption ("ast")) {
            astfile = new File(line.getOptionValue ("ast"));
            astfile.delete();
        }
        
        // Option -trace dotfile
        if (line.hasOption ("trace")) {
            File tracef = new File(line.getOptionValue("trace"));
            tracef.delete();
            tracefile = new PrintWriter(new FileWriter(tracef, true));
        }
        
        // Option -noexec
        if (line.hasOption ("noexec")) execute = false;

        // Remaining arguments (the input file)
        String[] files = line.getArgs();

        switch(files.length) {
            case 0:
                interactive = true;
                break;
            case 1:
                infile = files[0];
                break;
            default:
                System.err.println ("Incorrect command line.");
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp (cmdline, options);
                return false;
        }

        return true;
    }
}

