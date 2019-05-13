package time;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Used to get formatted current date and time Strings.
 */
public class DateTime {
    
    /**
     * Gets current date and time of format
     * Day dd.mm.yyyy at hh:mm PM/AM
     * eg.(Sun 01.11.2014 at 11:31 AM)
     * @return date and time in formatted string
     */
    public static String getDateTime() {
        Date date= new Date( );
        SimpleDateFormat f= new SimpleDateFormat("' ('E dd.MM.yyyy 'at' hh:mm a')'");
        return f.format(date);
    }
    
    /**
     * Gets current date and time of format yyyyMMddHHmmssSSS.Used for creating
     * time stamps for tags.
     * eg.(Sun 01.11.2014 at 11:31:22:335 AM would be 20141101113122335)
     * @return time stamp
     */
    public static String getFileTimeStamp() {
        Date date= new Date( );
        SimpleDateFormat f= new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return f.format(date);
    }
    
}
