package com.xprinter.posconsend.utils;

import java.io.UnsupportedEncodingException;

/**
 * Created by kylin on 2017/4/6.
 */

public class StringUtils {
    /**
     * 字符串转byte数组
     * */
    public static byte[] strTobytes(String str){
        byte[] b=null,data=null;
        try {
            b = str.getBytes("utf-8");
            data=new String(b,"utf-8").getBytes("gbk");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return data;
    }
}
