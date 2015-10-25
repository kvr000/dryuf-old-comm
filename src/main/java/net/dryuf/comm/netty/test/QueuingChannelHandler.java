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

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class QueuingChannelHandler<T> extends ChannelHandlerAdapter
{
	public				QueuingChannelHandler(BlockingQueue<T> queue)
	{
		this.queue = queue;
	}

	public				QueuingChannelHandler()
	{
		this.queue = new LinkedBlockingQueue<>();
	}

	public QueuingChannelHandler<T>	setInactiveMessage(T inactiveMessage)
	{
		this.inactiveMessage = inactiveMessage;
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void			channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
	{
		try {
			queue.put((T) msg);
		}
		finally {
			ReferenceCountUtil.release(msg);
		}
	}

	@Override
	public void			channelInactive(ChannelHandlerContext ctx) throws Exception
	{
		if (inactiveMessage != null)
			queue.put(inactiveMessage);
	}

	public BlockingQueue<T>		getQueue()
	{
		return queue;
	}

	protected final BlockingQueue<T> queue;

	protected T			inactiveMessage;
}
