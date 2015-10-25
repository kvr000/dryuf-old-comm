package net.dryuf.comm.netty.test.test;

import net.dryuf.comm.netty.test.StreamServerTester;
import net.dryuf.comm.server.netty.test.LineEchoStreamNettyServer;
import net.dryuf.tenv.AppTenvObject;
import net.dryuf.text.util.HexUtil;
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
import java.io.InputStream;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:testContext.xml")
public class StreamServerTesterTest extends AppTenvObject
{
	@SuppressWarnings("unchecked")
	@Test(timeout = 10000L)
	public void			testStreams()
	{
		try (StreamServerTester<LineEchoStreamNettyServer> tester = (StreamServerTester<LineEchoStreamNettyServer>)getAppContainer().createBeaned(StreamServerTester.class, null)) {
			tester.startServer(LineEchoStreamNettyServer.class);

			Channel client = tester.startClient();
			DataInputStream input = tester.getClientDataStream();

			client.writeAndFlush(Unpooled.wrappedBuffer("hi\n".getBytes(Charsets.UTF_8)));
			Assert.assertEquals('h', input.readByte());
			Assert.assertEquals('i', input.readByte());
			Assert.assertEquals('\n', input.readByte());
			client.writeAndFlush(Unpooled.wrappedBuffer("world\n".getBytes(Charsets.UTF_8)));
			Assert.assertEquals('w', input.readByte());
			Assert.assertEquals('o', input.readByte());
			Assert.assertEquals('r', input.readByte());
			Assert.assertEquals('l', input.readByte());
			Assert.assertEquals('d', input.readByte());
			Assert.assertEquals('\n', input.readByte());

			client.disconnect();
			Assert.assertEquals(-1, input.read());
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
