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

import com.google.common.util.concurrent.ListenableFuture;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.dryuf.comm.netty.NettyManager;
import net.dryuf.comm.server.netty.AbstractDatagramNettyServer;
import net.dryuf.comm.server.netty.AbstractStreamNettyServer;
import net.dryuf.core.AppContainer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.ReferenceCountUtil;

import javax.inject.Inject;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Created by rat on 08/10/2015.
 */
public class DatagramServerTester<SERVERT extends AbstractDatagramNettyServer> extends AbstractServerTester<SERVERT>
{
	public Channel                  startClient()
	{
		try {
			clientQueue = new LinkedBlockingQueue<>();
			Channel client = nettyTestManager.getNettyManager().createDatagramBootstrap()
				.handler(new ChannelInboundHandlerAdapter() {
					@Override
					public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
						try {
							DatagramPacket packet = (DatagramPacket) msg;
							clientQueue.put(packet.content().copy());
						}
						finally {
							ReferenceCountUtil.release(msg);
						}
					}

				})
				.connect(server.getLocalAddress())
				.sync()
				.channel();
			nettyTestManager.addManagedChannel(client);
			return client;
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public BlockingQueue<ByteBuf>   getClientQueue()
	{
		return clientQueue;
	}

	protected BlockingQueue<ByteBuf> clientQueue;
}
