package com.fsq.client;

import java.io.IOException;

/**
 * @author fsq
 * @date 2020/01/15
 * 启动客户端二
 */
public class ClientB {

  public static void main(String[] args) throws IOException {

    new NioClient("林伟翔").start();

  }

}
