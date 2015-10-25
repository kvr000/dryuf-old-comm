package net.dryuf.comm.netty.codec.test;

import net.dryuf.comm.netty.codec.CumulatingStringBuilderHandler;
import net.dryuf.comm.netty.test.NettyTestManager;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import org.junit.Assert;
import org.junit.Test;
import org.apache.commons.codec.Charsets;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class CumulatingStringBuilderHandlerTest
{
	@Test(timeout = 10000L)
	public void                     testRepeatedPass() throws Exception
	{
		try (NettyTestManager netty = new NettyTestManager()) {
			BlockingQueue<StringBuilder> syncer = new LinkedBlockingQueue<>();
			Channel channel = netty.startStreamServer(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline()
						.addLast(new CumulatingStringBuilderHandler())
						.addLast(new ChannelHandlerAdapter() {
							@Override
							public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
								syncer.put((StringBuilder) msg);
							}

							@Override
							public void channelInactive(ChannelHandlerContext ctx) throws Exception {
								syncer.put(new StringBuilder());
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
			Assert.assertEquals("0", syncer.take().toString());
			client.writeAndFlush(Unpooled.wrappedBuffer("123\n456\n".getBytes(Charsets.UTF_8)));
			StringBuilder msg = syncer.take();
			Assert.assertEquals("0123\n456\n", msg.toString());
			msg.delete(0, 5);
			client.writeAndFlush(Unpooled.wrappedBuffer("789\n".getBytes(Charsets.UTF_8)));
			Assert.assertEquals("456\n789\n", syncer.take().toString());
			client.disconnect();
			Assert.assertEquals("", syncer.take().toString());
		}
	}
}
