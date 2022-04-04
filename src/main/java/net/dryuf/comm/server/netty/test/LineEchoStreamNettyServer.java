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

package net.dryuf.comm.server.netty.test;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import com.google.common.net.HostAndPort;

import io.netty.channel.ChannelInboundHandlerAdapter;
import net.dryuf.comm.server.netty.AbstractStreamNettyServer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;


public class LineEchoStreamNettyServer extends AbstractStreamNettyServer
{
	public				LineEchoStreamNettyServer()
	{
	}

	protected ChannelHandler	createListenerHandler()
	{
		return new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(new LineBasedFrameDecoder(1024));
				ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
					@Override
					public void			channelRead(ChannelHandlerContext ctx, Object msg) {
						try {
							ByteBuf resp = ctx.alloc().buffer(16);
							resp.writeBytes((ByteBuf)msg).writeByte('\n');
							ctx.writeAndFlush(resp);
						}
						finally {
							ReferenceCountUtil.release(msg);
						}
					}
				});
			}
		};
	}
}
