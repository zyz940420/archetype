package ${package}.util;

import org.apache.commons.lang3.StringUtils;

public class StringUtil {
    public static String nvl(String ...value){
        if(value.length==0){
            return null;
        }
        for(String s:value){
            if(!StringUtils.isBlank(s)){
                return s;
            }
        }
        return null;
    }
}
