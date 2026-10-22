package utils;

import java.text.SimpleDateFormat;
import java.util.Date;
public class Logger{

    public static final String RESET    = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN    = "\u001B[32m";
    //public static final String YELLOW  = "\u001B[33m";
    //public static final String BLUE     = "\u001B[34m";

    private static String getTimeStamp(){
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }
    public static void error(String msg){
        System.out.println("| " + RED + "ERROR" + RESET + " | " + getTimeStamp() + " | " + msg);
    }

    public static void info(String msg){
        System.out.println("| " + GREEN + "INFO" + RESET + "  | " + getTimeStamp() + " | " + msg);
    }


}
