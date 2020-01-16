package com.fsq.server;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author fsq
 * @date 2020/01/15 服务器主程序
 */
public class WeChatServer {

  private static final int BUF_SIZE = 1024;
  private static final int PORT = 8888;
  /**
   * 用来判断split后数组大小，防止NPE
   */
  private static final int ARRAYS_SIZE = 3;
  private static Map<String, Object> user = new HashMap<>();

  private Selector selector;
  private ServerSocketChannel ssc;


  public WeChatServer() {
    try {
      init();
      System.out.println("服务器启动成功！");
      selector();
    } catch (IOException e) {
      System.out.println("初始化失败");
    }
  }

  private void init() throws IOException {

    selector = Selector.open();
    ssc = ServerSocketChannel.open();
    ssc.socket().bind(new InetSocketAddress("127.0.0.1", PORT));
    ssc.configureBlocking(false);
    ssc.accept();
    ssc.register(selector, SelectionKey.OP_ACCEPT);

  }

  public static void main(String[] args) {
    new WeChatServer();
  }

  public void selector() {

    try {

      while (true) {
        if (selector.select() == 0) {
          continue;
        }
        Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
        while (iter.hasNext()) {
          SelectionKey key = iter.next();
          iter.remove();
          if (key.isAcceptable()) {
            handleAccept();
          }
          if (key.isValid() && key.isReadable()) {
            handleRead(key);
          }

        }
      }

    } catch (IOException e) {
      System.out.println("客户端非正常退出");

      e.printStackTrace();
    }
  }

  private void handleAccept() throws IOException {
    //接收客户端SockerChannel，注册监听读取事件
    SocketChannel sc = ssc.accept();
    sc.configureBlocking(false);
    sc.register(selector, SelectionKey.OP_READ);


  }

  private void handleRead(SelectionKey key) {

    SocketChannel sc = (SocketChannel) key.channel();
    try {

      ByteBuffer buf = ByteBuffer.allocate(BUF_SIZE);
      StringBuffer sb = new StringBuffer();
      int bytesRead = sc.read(buf);
      // 防止无限抛异常
      if (bytesRead == -1) {
        throw new IOException();
      }
      while (bytesRead > 0) {
        buf.flip();
        sb.append(Charset.forName("UTF-8").decode(buf));
        buf.clear();
        bytesRead = sc.read(buf);
      }

      // 保存客户端名字！！
      if (sb.toString().indexOf("sendName") >= 0) {
        user.put(sb.toString().substring(8), sc);
        return;
      }

      // 如果是群聊，广播信息
      if (sb.length() > 0) {
        broadCast(sc, sb.toString());
        System.out.println(sb.toString());

      }
    } catch (IOException e) {
      try {
        sc.close();
      } catch (IOException e1) {

      }

      System.out.println("断开连接.");
    }

  }

  private void broadCast(SocketChannel sourceChannel, String request) {

    Set<SelectionKey> selectionKeySet = selector.keys();
    //  man-send-woman-aaaaaa
    boolean flag = request.contains("-send-");
    if (flag) {
      String[] context = request.split("-");
      if (context.length >= ARRAYS_SIZE) {
        String sendName = context[0];
        String receiveName = context[2];
        Object reChannel = user.get(receiveName);
        if (reChannel != null && reChannel instanceof SocketChannel) {
          try {
            if (context[3] == null) {
              context[3] = " ";
            }
            if (sendName.equals(receiveName)) {
              ((SocketChannel) reChannel).write(Charset.forName("UTF-8").encode("不能私聊自己."));
            }
            String words = "(私聊):" + sendName + "对你说: " + context[3];
            ((SocketChannel) reChannel).write(Charset.forName("UTF-8").encode(words));
          } catch (IOException e) {
            e.printStackTrace();

          }
        }
      }
    } else {

      /**
       * 循环向所有channel广播信息
       */
      String con = request.replaceFirst("-", ":");

      selectionKeySet.forEach(selectionKey -> {
        Channel targetChannel = selectionKey.channel();

        // 剔除发消息的客户端
        if (targetChannel instanceof SocketChannel
            && targetChannel != sourceChannel) {
          try {
            // 将信息发送到targetChannel客户端
            ((SocketChannel) targetChannel).write(Charset.forName("UTF-8").encode(con));

          } catch (IOException e) {
            System.out.println("未知错误2");
            e.printStackTrace();
          }
        }
      });
    }
  }


}
