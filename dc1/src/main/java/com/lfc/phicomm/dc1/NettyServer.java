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
	 * �洢���ӵ�TCP�ͻ�����Ϣ
	 */
	public static Map<String, ChannelHandlerContext> tcpClient = new HashMap<String, ChannelHandlerContext>();
	
	public static Map<String, String> tcpReceive = new HashMap<String, String>();
	
	public static boolean requestFlag = false;
	/*
	 *  boss �߳������ڴ������ӹ���
	 */
	private EventLoopGroup boss = new NioEventLoopGroup();
	
	/*
	 * work �߳����������ݴ���
	 */
	private EventLoopGroup work = new NioEventLoopGroup();
	
	/*
	 * tcp�������˿�
	 */
	int port = 8000;

	@PostConstruct
	public void start() throws InterruptedException {
		ServerBootstrap bootstrap = new ServerBootstrap();
		bootstrap.group(boss, work)
			// ָ��channel
			.channel(NioServerSocketChannel.class)
			// ʹ��ָ���˿������׽��ֵ�ַ
			.localAddress(new InetSocketAddress(port))
			// ����˿����Ӷ���������ӦTCP/IPЭ��listen������backlog����
			.option(ChannelOption.SO_BACKLOG, 1024)
			// ����TCP�����ӣ�һ���������Сʱ��û������ͨ���ǣ�TCP���Զ�����һ���̽�����ݱ���
//			.childOption(ChannelOption.SO_KEEPALIVE, true)
			// ���������ݰ���װ�ɸ����֡���д��ͣ��������ĸ���
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
	 * ��TCP�ͻ��˷�����Ϣ
	 */
	public static void sendStrToClient(String sendStr) {
		ChannelHandlerContext ctx = NettyServer.tcpClient.get("clientCtx");
		if(ctx == null) {
			log.error("�Ҳ���TCP�ͻ���");
		}else {
			ctx.write(sendStr);
			ctx.flush();
			log.info("��TCP�ͻ���" + NettyServer.getAddressFromContext(ctx) + "�����ַ�����" + sendStr);
		}
	}
	
	/**
	 * ����ChannelHandleContext�õ��ͻ��˵�ip�Ͷ˿�
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
	 * ��̨DC1�õ����͸�����˵�����
	 * @return
	 */
	public static String getReceiveString() {
		ChannelHandlerContext ctx = tcpClient.get("clientCtx");
		if(ctx == null)  {
			log.error("��TCP�ͻ�������");
			return null;
		}
		String address = getAddressFromContext(ctx);
		return tcpReceive.get(address);
	}
}
