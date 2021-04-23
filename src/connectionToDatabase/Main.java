package connectionToDatabase;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws SQLException, IOException {
        Homework homework = new Homework();
        homework.setConnection("root", "password");
        //Exercise 2
       // homework.getVillainsNamesEx2();

        //Exercise 3
       // homework.   getMinionsNamesEx3();


        //Exercise 4
      //  homework.addMinionEx4();

        //Exercise 5
      // homework.changeTownNameCasingEx5();

        // Exercise 6
      //  homework.removeVillianEx6();

        //Exercise 7
        homework.printMinionsEx7();

       // homework.increaseAgeWithStoreProcedure();

    }
}
