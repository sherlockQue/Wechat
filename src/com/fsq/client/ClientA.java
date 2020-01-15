package com.fsq.client;

import java.io.IOException;

/**
 * @author fsq
 * @date 2020/01/15
 * 启动客户端一
 */
public class ClientA {


  public static void main(String[] args) throws IOException {

    new NioClient("刘青松").start();

  }

}
