package com.fsq.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 *
 * 保存聊天记录文件
 * 各自保存自己的
 */
public class SaveRecords {

  private String dir = "D:/test/";
  private String name;
  private File file ;

  public SaveRecords(String name) {
    this.name = name;
    dir = dir + name + ".txt";
    this.file = new File(dir);
    if (file.exists()) {
      try {
        file.mkdirs();
        file.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }

    }

  }

  public void WriteFile(String text) throws IOException{

    try {
      //FileOutputStream in= new FileOutputStream(file);
      OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file,true));

      try {
        out.write(text);
        out.close();
      } catch (IOException e1) {
        e1.printStackTrace();
        System.out.println("写入异常");
        out.close();

      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      System.out.println("文件为null");


    }


  }



}
