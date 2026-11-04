package utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import apps.BaseApp;

public class Logger{

    public static boolean userInputLoopActive = false;
    public static final String userInputString = BaseApp.getUserInputRequest();
    public static final String RESET    = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN    = "\u001B[32m";
    //public static final String YELLOW  = "\u001B[33m";
    //public static final String BLUE     = "\u001B[34m";

    private static String getTimeStamp(){
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }
    public static void error(String msg){
        if (!userInputLoopActive){
            System.out.println("| " + RED + "ERROR" + RESET + " | " + getTimeStamp() + " | " + msg);
        }
        else{
            int difference = userInputString.length() - msg.length();
            String padding = difference > 0 ? " ".repeat(difference) : "";
            System.out.println("\r| " + RED + "ERROR" + RESET + " | " + getTimeStamp() + " | " + msg + padding);
            System.out.print(BaseApp.getUserInputRequest());
        }
    }

    public static void info(String msg){
        if (!userInputLoopActive){
            System.out.println("| " + GREEN + "INFO" + RESET + "  | " + getTimeStamp() + " | " + msg);
        }
        else{
            int difference = userInputString.length() - msg.length();
            String padding = difference > 0 ? " ".repeat(difference) : "";
            System.out.println("\r| " + GREEN + "INFO" + RESET + "  | " + getTimeStamp() + " | " + msg + padding);
            System.out.print(BaseApp.getUserInputRequest());
        }
    }


}
