package net.dryuf.comm.netty.test.test;

import net.dryuf.comm.netty.test.DatagramServerTester;
import net.dryuf.comm.netty.test.NettyTestManager;
import net.dryuf.comm.netty.test.StreamServerTester;
import net.dryuf.comm.server.netty.test.EchoDatagramNettyServer;
import net.dryuf.comm.server.netty.test.LineEchoStreamNettyServer;
import net.dryuf.tenv.AppTenvObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.apache.commons.codec.Charsets;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:testContext.xml")
public class DatagramServerTesterTest extends AppTenvObject
{
	@SuppressWarnings("unchecked")
	@Test(timeout = 10000L)
	public void			testDatagram()
	{
		try (DatagramServerTester<EchoDatagramNettyServer> tester = (DatagramServerTester<EchoDatagramNettyServer>)getAppContainer().createBeaned(DatagramServerTester.class, null)) {
			tester.startServer(EchoDatagramNettyServer.class);

			Channel client = tester.startClient();
			BlockingQueue<ByteBuf> input = tester.getClientQueue();

			client.writeAndFlush(Unpooled.wrappedBuffer("hi\n".getBytes(Charsets.UTF_8)));
			NettyTestManager.assertByteBufEquals(Unpooled.wrappedBuffer("hi\n".getBytes(Charsets.UTF_8)), input.take());
			client.writeAndFlush(Unpooled.wrappedBuffer("world\n".getBytes(Charsets.UTF_8)));
			NettyTestManager.assertByteBufEquals(Unpooled.wrappedBuffer("world\n".getBytes(Charsets.UTF_8)), input.take());
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
