package com.generaterun.foo;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
/**
 * @author pinghui.zhang
 *
 */
public class CmdUtils {
	/** 
     * 读取控制命令的输出结果 
     * 
     * @param cmd                命令 
     * @param dir 命令的根路径
     * @param isPrettify 返回的结果是否进行美化（换行），美化意味着换行，默认不进行美化,当此参数为null时也不美化， 
     * @return 控制命令的输出结果 
     * @throws IOException
     */ 
    public static String readConsole(String cmd, File dir,Boolean isPrettify) throws IOException { 
            StringBuffer cmdout = new StringBuffer(); 
            Process process = Runtime.getRuntime().exec(cmd,null,dir);     //执行一个系统命令 
            InputStream fis = process.getInputStream(); 
            BufferedReader br = new BufferedReader(new InputStreamReader(fis,"UTF-8")); 
            String line = null; 
            if (isPrettify == null || !isPrettify) { 
                    while ((line = br.readLine()) != null) { 
                            cmdout.append(line); 
                    } 
            } else { 
                    while ((line = br.readLine()) != null) { 
                            cmdout.append(line).append(System.getProperty("line.separator")); 
                    } 
            }
            if(cmdout.toString().length()>0){
//            	System.out.println("执行系统命令后的结果为：\n" + cmdout.toString()); 
            }else{
            	 br = new BufferedReader(new InputStreamReader(process.getErrorStream(),"UTF-8")); 
                 if (isPrettify == null || !isPrettify) { 
                         while ((line = br.readLine()) != null) { 
                                 cmdout.append(line); 
                         } 
                 } else { 
                         while ((line = br.readLine()) != null) { 
                                 cmdout.append(line).append(System.getProperty("line.separator")); 
                         } 
                 }
                 System.out.println("命令执行出错：\n" + cmdout.toString()); 
            }
            return cmdout.toString().trim(); 
    } 
    
}