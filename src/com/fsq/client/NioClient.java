package com.fsq.client;


import com.fsq.util.SaveRecords;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * 客户端类 启动类在ClientA，ClientB
 *
 * @author fsq
 * @date 2020/01/15
 */
public class NioClient {

  private String name;
  private static final int PORT = 8888;
  private Selector selector;
  private SocketChannel sc;
  private SaveRecords saveRecords;

  public NioClient(String name) throws IOException {
    this.name = name;
    //初始化参数
    init();
    System.out.println("启动成功，你的名字：" + this.name
        + "  输入 ”quit“ 退出， 默认群聊，私聊请按照格式： ”send-name-XXXX(内容)“， 例：私聊 Jerry， send-Jerry-hello world!");
  }

  private void init() {
    this.saveRecords = new SaveRecords(name);
    try {
      selector = Selector.open();
      sc = SocketChannel.open(new InetSocketAddress("127.0.0.1", PORT));
      sc.configureBlocking(false);
      sc.register(selector, SelectionKey.OP_READ);
      sendName();
    } catch (IOException e) {
      System.out.println("初始失败");
    }


  }

  public void start() {

    try {

      //启动一个线程监听服务器消息
      RunClient run = new RunClient(selector, saveRecords);
      new Thread(run).start();
      //创建记录类

      BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

      while (true) {
        String request = input.readLine();

        //输入quit，退出，关闭线程
        if ("quit".equalsIgnoreCase(request)) {
          run.setFlag();
          selector.close();
          sc.close();

          throw new IOException();
        }

        if (request != null && request.length() > 0) {

          String sendText = name + "-" + request;

          sc.write(Charset.forName("utf-8").encode(sendText));
          //  me-send-she-aaaaaa

          String words = "我说: " + request;

          if (sendText.contains("-send-")) {
            String[] context = sendText.split("-");
            words = "（私聊） 我对" + context[2] + "说: " + context[3];
          }

          System.out.println(words);
          saveRecords.writeFile(words + "\n");
        }
      }
    } catch (IOException e) {
      System.out.println("已退出");
    }

  }

  private void sendName() {

    try {
      sc.write(Charset.forName("utf-8").encode("sendName" + name));
    } catch (IOException e) {
      System.out.println("初始姓名失败");
    }

  }

}

/**
 * 线程类，用于接收服务器信息
 */
class RunClient implements Runnable {

  private Selector selector;
  private SaveRecords saveRecords;
  private static final int BUF_SIZE = 1024;
  public boolean flag = true;

  public RunClient(Selector selector, SaveRecords saveRecords) {
    this.selector = selector;
    this.saveRecords = saveRecords;
  }

  public void setFlag() {
    flag = false;
  }

  @Override
  public void run() {

    try {
      while (flag) {

        if (selector.select() <= 0) {
          continue;
        }
        Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
        while (iter.hasNext()) {
          SelectionKey key = iter.next();
          if (key.isValid() && key.isReadable()) {
            readHandler(key);
          }
          iter.remove();
        }
      }

    } catch (IOException e) {
      System.out.println("已断开连接");
    } catch (Exception e) {
      System.out.println("非正常退出");
    }

  }

  public void readHandler(SelectionKey key) throws IOException {
    SocketChannel sc = (SocketChannel) key.channel();
    ByteBuffer byteBuffer = ByteBuffer.allocate(BUF_SIZE);

    StringBuffer sb = new StringBuffer();
    while (sc.read(byteBuffer) > 0) {

      byteBuffer.flip();
      sb.append(Charset.forName("UTF-8").decode(byteBuffer));
    }
    // sc.close();
    System.out.println(sb.toString());
    saveRecords.writeFile(sb.toString() + "\n");
  }

}
