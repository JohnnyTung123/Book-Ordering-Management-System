import java.awt.*;
import java.text.*;
import java.util.*;
import javax.swing.*;

public class MyFrame {

    public static void start1(String[] myArgs) {
        Calendar calendar;
        SimpleDateFormat timeFormat;
        SimpleDateFormat dayFormat;
        SimpleDateFormat dateFormat;
        JLabel timeLabel;
        JLabel dayLabel;
        JLabel dateLabel;
        String time;
        String day;
        String date;
        timeFormat = new SimpleDateFormat("hh:mm:ss a");
        dayFormat = new SimpleDateFormat("EEEE");
        dateFormat = new SimpleDateFormat("MMMMM dd, yyyy");
        while (true) {
            time = timeFormat.format(Calendar.getInstance().getTime());

            day = dayFormat.format(Calendar.getInstance().getTime());

            date = dateFormat.format(Calendar.getInstance().getTime());
            System.out.print("\r" + date + " " + day + " " + time);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}