package ir.sharif.aichallenge.towerDefence;

/**
 * Created by msi1 on 1/24/2018.
 */
public class InvalidEventException extends Exception {
    public InvalidEventException(String message) {
        super(message);
//        this.print(message);
    }

    public InvalidEventException() {
    }

    public void print(String message) {
        System.err.println(message);
    }
}
