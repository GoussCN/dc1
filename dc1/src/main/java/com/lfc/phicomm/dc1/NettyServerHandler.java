package com.lfc.phicomm.dc1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class NettyServerHandler extends ChannelInboundHandlerAdapter {
	
	private static Logger log = LoggerFactory.getLogger(NettyServerHandler.class);

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		NettyServer.tcpClient.put("clientCtx", ctx);
		log.info("channel active: " + NettyServer.getAddressFromContext(ctx));
//		NettyServer.sendStrToClient("WELCOME CONTECTED");
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		log.info("channel read: " + msg.toString());
		NettyServer.tcpReceive.put(NettyServer.getAddressFromContext(ctx), msg.toString());
		JSONObject msgObj = JSONObject.parseObject(msg.toString());
		if(!msgObj.isEmpty() && "activate=".equals(msgObj.getString("action"))) {
			NettyServer.requestFlag = true;
		}else {
			NettyServer.requestFlag = false;
		}
		super.channelRead(ctx, msg);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.error("channel exception .....");
		super.exceptionCaught(ctx, cause);
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		log.info("channel registered");
		super.channelRegistered(ctx);
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		log.info("channel unregistered");
		super.channelUnregistered(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		log.info("channel inactive");
		super.channelInactive(ctx);
	}
	
}