package com.lfc.phicomm.dc1;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {

	 @Override
	 protected void initChannel(SocketChannel ch) throws Exception {
	  // Ìí¼Ó±à½âÂë
	  ch.pipeline().addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
	  ch.pipeline().addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
	  ch.pipeline().addLast(new NettyServerHandler());
	 }
	 
}