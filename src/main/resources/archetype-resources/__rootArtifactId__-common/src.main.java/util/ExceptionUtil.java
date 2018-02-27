package ${package}.util;

public class ExceptionUtil {
    /**
     * @param e 异常
     * @return 异常栈
     */
    public static String getExceptionStack(Exception e) {
        StringBuffer sb = new StringBuffer();

        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
            sb.append("\t" + stackTraceElement.toString() + "\n");
        }
        sb.append("\t"+e.getCause().getMessage());
        return sb.toString();
    }
}
