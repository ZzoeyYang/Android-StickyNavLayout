package online.sniper.utils.log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * ===============================================
 * DEVELOPER : RenYang <br/>
 * DATE : 2016/11/1 <br/>
 * DESCRIPTION :
 */
public class LogFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {
        StringBuffer sb = new StringBuffer();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        sb.append("日期时间：");
        sb.append(format.format(new Date()));
        sb.append("\n");
        sb.append(record.getMessage());
        sb.append("\n\n");
        return sb.toString();
    }

}
