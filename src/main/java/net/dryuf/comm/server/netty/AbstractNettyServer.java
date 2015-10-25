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

package net.dryuf.comm.server.netty;

import net.dryuf.comm.netty.NettyManager;
import net.dryuf.comm.netty.concurrent.spring.SpringForwardingNettyChannelLifecycleFutureListener;
import net.dryuf.comm.server.AbstractAsyncServer;
import io.netty.channel.ChannelFuture;

import javax.inject.Inject;
import java.net.SocketAddress;


public abstract class AbstractNettyServer extends AbstractAsyncServer implements Runnable
{
	public				AbstractNettyServer()
	{
	}

	protected abstract ChannelFuture createChannel();

	public synchronized void	prepare()
	{
		if (terminated)
			return;
		super.prepare();
		serverChannelFuture = createChannel();
		serverChannelFuture.addListener(new SpringForwardingNettyChannelLifecycleFutureListener<>(asyncServerFuture));
	}

	@Override
	public void			run()
	{
		prepare();
	}

	public synchronized  void	terminate()
	{
		terminated = true;
		if (serverChannelFuture != null) {
			serverChannelFuture.syncUninterruptibly().channel().close();
		}
		else {
			asyncServerFuture.set(Integer.valueOf(0));
		}
	}

	public SocketAddress		getLocalAddress()
	{
		try {
			return serverChannelFuture.sync().channel().localAddress();
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Inject
	protected NettyManager		nettyManager;

	protected ChannelFuture		serverChannelFuture;

	protected boolean		terminated = false;
}
