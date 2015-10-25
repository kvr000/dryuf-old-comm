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

package net.dryuf.comm.netty;


import net.dryuf.comm.MessageProcessor;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.ReferenceCountUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;

public abstract class DatagramNettyHandlerMessageProcessorAdapter<OUT, IN> extends ChannelHandlerAdapter
{
	public				DatagramNettyHandlerMessageProcessorAdapter(Executor executor, MessageProcessor<OUT, IN> processor)
	{
		this.executor = executor;
		this.processor = processor;
	}

	public abstract IN		convertDatagram(DatagramPacket packet);

	public abstract DatagramPacket	formatDatagram(InetSocketAddress peerAddress, OUT message);

	@Override
	public void			channelRead(ChannelHandlerContext ctx, Object msg)
	{
		DatagramPacket incoming = (DatagramPacket)msg;
		final InetSocketAddress peerAddress = incoming.sender();
		IN packet = convertDatagram(incoming);
		try {
			executor.execute(() -> {
				for (OUT response : processor.processMessage(packet)) {
					ctx.write(formatDatagram(peerAddress, response));
				}
				ctx.flush();
			});
		}
		finally {
			ReferenceCountUtil.release(msg);
		}
	}

	@Override
	public void			exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
	{
		// Close the connection when an exception is raised.
		logger.error("Processing packet failed, closing connection: "+cause.toString(), cause);
	}

	protected Executor		executor;

	protected MessageProcessor<OUT, IN> processor;

	protected Logger		logger = LogManager.getLogger(getClass());
}
