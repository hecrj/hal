package hal.interpreter.exceptions;


public class InvalidArgumentsException extends RuntimeException
{
    public InvalidArgumentsException() {
        super("Invalid number of arguments");
    }
}
