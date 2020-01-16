package com.fsq.client;


import java.io.IOException;

/**
 * @author fsq
 * @date 2020/01/16
 * 启动客户端三
 */
public class ClientC {
  public static void main(String[] args) throws IOException {

    new NioClient("doinb").start();

  }

}
