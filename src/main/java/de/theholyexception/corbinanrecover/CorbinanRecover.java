package de.theholyexception.corbinanrecover;

public class CorbinanRecover {

    public static void main(String[] args) {
        //File folder = new File(args[0]);

        String s = (args.length > 0 ? args[0] : null);
        String t = (args.length > 1 ? args[1] : null);
        String p = (args.length > 2 ? args[2] : null);



        new MainWindow(s,t,p);
    }



}

