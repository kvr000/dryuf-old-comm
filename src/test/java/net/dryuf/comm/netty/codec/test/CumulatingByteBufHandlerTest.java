package net.dryuf.comm.netty.codec.test;

import net.dryuf.comm.netty.codec.CumulatingByteBufHandler;
import net.dryuf.comm.netty.test.NettyTestManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class CumulatingByteBufHandlerTest
{
	@Test(timeout = 10000L)
	public void                     testRepeatedPass() throws Exception
	{
		try (NettyTestManager netty = new NettyTestManager()) {
			BlockingQueue<ByteBuf> syncer = new LinkedBlockingQueue<>();
			Channel channel = netty.startStreamServer(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline()
						.addLast(new CumulatingByteBufHandler())
						.addLast(new ChannelHandlerAdapter() {
							@Override
							public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
								syncer.put(Unpooled.copiedBuffer((ByteBuf) msg));
							}

							@Override
							public void channelInactive(ChannelHandlerContext ctx) throws Exception {
								syncer.put(Unpooled.copiedBuffer(new byte[0]));
							}
						});
				}
			}).channel();
			netty.addManagedChannel(channel);

			Channel client = netty.getNettyManager().createStreamBootstrap()
				.handler(new ChannelHandlerAdapter())
				.connect(channel.localAddress())
				.sync()
				.channel();
			netty.addManagedChannel(client);

			client.writeAndFlush(Unpooled.wrappedBuffer(new byte[]{ '0' }));
			NettyTestManager.assertByteBufEquals(Unpooled.wrappedBuffer("0".getBytes(StandardCharsets.UTF_8)), syncer.take());
			client.writeAndFlush(Unpooled.wrappedBuffer("123\n456\n".getBytes(StandardCharsets.UTF_8)));
			NettyTestManager.assertByteBufEquals(Unpooled.wrappedBuffer("0123\n456\n".getBytes(StandardCharsets.UTF_8)), syncer.take());
			client.disconnect();
			NettyTestManager.assertByteBufEquals(Unpooled.wrappedBuffer("".getBytes(StandardCharsets.UTF_8)), syncer.take());
		}
	}
}
