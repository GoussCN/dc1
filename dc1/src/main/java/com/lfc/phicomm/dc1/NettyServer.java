package com.lfc.phicomm.dc1;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

@Component
public class NettyServer {
	
	private static Logger log = LoggerFactory.getLogger(NettyServer.class);
	
	/*
	 * 存储连接的TCP客户端信息
	 */
	public static Map<String, ChannelHandlerContext> tcpClient = new HashMap<String, ChannelHandlerContext>();
	
	public static Map<String, String> tcpReceive = new HashMap<String, String>();
	
	public static boolean requestFlag = false;
	/*
	 *  boss 线程组用于处理连接工作
	 */
	private EventLoopGroup boss = new NioEventLoopGroup();
	
	/*
	 * work 线程组用于数据处理
	 */
	private EventLoopGroup work = new NioEventLoopGroup();
	
	/*
	 * tcp服务器端口
	 */
	int port = 8000;

	@PostConstruct
	public void start() throws InterruptedException {
		ServerBootstrap bootstrap = new ServerBootstrap();
		bootstrap.group(boss, work)
			// 指定channel
			.channel(NioServerSocketChannel.class)
			// 使用指定端口设置套接字地址
			.localAddress(new InetSocketAddress(port))
			// 服务端可连接队列数，对应TCP/IP协议listen函数中backlog参数
			.option(ChannelOption.SO_BACKLOG, 1024)
			// 设置TCP长连接，一般如果两个小时内没有数据通信是，TCP会自动发送一个活动探测数据报文
//			.childOption(ChannelOption.SO_KEEPALIVE, true)
			// 将晓得数据包包装成更大的帧进行传送，提高网络的负载
			.childOption(ChannelOption.TCP_NODELAY, true)
			
			.childHandler(new ServerChannelInitializer());
		ChannelFuture future = bootstrap.bind().sync();
		if(future.isSuccess()) {
			log.info("netty server start success");
		}
	}
	
	@PreDestroy
	public void destory() throws InterruptedException {
		boss.shutdownGracefully().sync();
		work.shutdownGracefully().sync();
		log.info("netty server closed");
	}

	/**
	 * 向TCP客户端发送消息
	 */
	public static void sendStrToClient(String sendStr) {
		ChannelHandlerContext ctx = NettyServer.tcpClient.get("clientCtx");
		if(ctx == null) {
			log.error("找不到TCP客户端");
		}else {
			ctx.write(sendStr);
			ctx.flush();
			log.info("向TCP客户端" + NettyServer.getAddressFromContext(ctx) + "发送字符串：" + sendStr);
		}
	}
	
	/**
	 * 根据ChannelHandleContext得到客户端的ip和端口
	 * @param context
	 * @return
	 */
	public static String getAddressFromContext(ChannelHandlerContext context) {
		InetSocketAddress inetSocketAddress = (InetSocketAddress)context.channel().remoteAddress();
		String clientIp = inetSocketAddress.getAddress().getHostAddress();
		int clientPort = inetSocketAddress.getPort();
		return clientIp + ":" + clientPort;
	}
	
	/**
	 * 单台DC1得到发送给服务端的数据
	 * @return
	 */
	public static String getReceiveString() {
		ChannelHandlerContext ctx = tcpClient.get("clientCtx");
		if(ctx == null)  {
			log.error("无TCP客户端连接");
			return null;
		}
		String address = getAddressFromContext(ctx);
		return tcpReceive.get(address);
	}
}
