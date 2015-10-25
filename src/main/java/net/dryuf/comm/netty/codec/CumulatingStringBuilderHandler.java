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

package net.dryuf.comm.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.codec.Charsets;


/**
 * Created by rat on 2015-09-20.
 */
public class CumulatingStringBuilderHandler extends ChannelHandlerAdapter
{
	@Override
	public void			channelRead(ChannelHandlerContext ctx, Object msg)
	{
		ByteBuf input = (ByteBuf) msg;
		try {
			cumulator.append(input.toString(Charsets.UTF_8));
		}
		finally {
			ReferenceCountUtil.release(input);
		}
		ctx.fireChannelRead(cumulator);
	}

	@Override
	public void                     write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
	{
		if (msg instanceof String) {
			ctx.write(((String)msg).getBytes(Charsets.UTF_8));
		}
		else if (msg instanceof StringBuilder) {
			ctx.write(msg.toString().getBytes(Charsets.UTF_8));
		}
		else if (msg instanceof ByteBuf) {
			ctx.write(msg);
		}
		else {
			throw new IllegalArgumentException("Invalid object passed to write, supported are String, StringBuilder and ByteBuf: "+msg.getClass().getName());
		}
	}

	protected StringBuilder         cumulator = new StringBuilder();
}
