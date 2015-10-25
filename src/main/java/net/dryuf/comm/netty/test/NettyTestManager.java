/*
 * Dryuf framework
 *
 * ----------------------------------------------------------------------------------
 *
 * Copyright (C) 2000-2015 Zbyněk Vyškovský
 *
 * ----------------------------------------------------------------------------------
 *
 * LICENSE:
 *
 * This file is part of Dryuf
 *
 * Dryuf is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * Dryuf is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Dryuf; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * @author	2000-2015 Zbyněk Vyškovský
 * @link	mailto:kvr@matfyz.cz
 * @link	http://kvr.matfyz.cz/software/java/dryuf/
 * @link	http://github.com/dryuf/
 * @license	http://www.gnu.org/licenses/lgpl.txt GNU Lesser General Public License v3
 */

package net.dryuf.comm.netty.test;

import net.dryuf.comm.netty.NettyManager;
import net.dryuf.comm.netty.NettyManagerImpl;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import org.junit.Assert;

import java.util.LinkedList;
import java.util.List;


/**
 * Created by rat on 08/10/2015.
 */
public class NettyTestManager extends java.lang.Object implements AutoCloseable
{
	public ChannelFuture            startStreamServer(ChannelInitializer<SocketChannel> channelInitializer)
	{
		try {
			return nettyManager.createListenerBootstrap()
				.childHandler(channelInitializer)
				.childOption(ChannelOption.SO_KEEPALIVE, true)
				.childOption(ChannelOption.SO_REUSEADDR, true)
				.bind("localhost", 0)
				.sync();
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public void                     closeChannel(Channel channel)
	{
		channel.close().awaitUninterruptibly();
	}

	public void                     addManagedChannel(Channel channel)
	{
		managedChannels.add(channel);
	}

	public static void		assertByteBufEquals(ByteBuf expected, ByteBuf actual)
	{
		Assert.assertEquals(expected.writerIndex(), actual.writerIndex());
		for (int i = 0; i < expected.writerIndex(); ++i) {
			Assert.assertEquals(expected.getByte(i), actual.getByte(i));
		}
	}

	public NettyManager             getNettyManager()
	{
		return nettyManager;
	}

	@Override
	public void                     close()
	{
		List<ChannelFuture> closeFutures = new LinkedList<>();
		for (Channel channel: managedChannels) {
			closeFutures.add(channel.close());
		}
		for (ChannelFuture closeFuture: closeFutures) {
			closeFuture.awaitUninterruptibly();
		}
		nettyManager.close();
	}

	protected NettyManager          nettyManager = new NettyManagerImpl();

	protected List<Channel>         managedChannels = new LinkedList<>();
}
