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

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;


public class NettyManagerImpl extends java.lang.Object implements NettyManager
{
	public 				NettyManagerImpl()
	{
		init();
	}

	public void			init()
	{
		bossGroup = new NioEventLoopGroup();
		workerGroup = new NioEventLoopGroup();
	}

	@Override
	public Bootstrap		createDatagramBootstrap()
	{
		return new Bootstrap()
			.group(bossGroup)
			.channel(NioDatagramChannel.class);
	}

	@Override
	public Bootstrap		createStreamBootstrap()
	{
		return new Bootstrap()
			.group(workerGroup)
			.channel(NioSocketChannel.class);
	}

	@Override
	public ServerBootstrap		createListenerBootstrap()
	{
		return new ServerBootstrap()
			.group(bossGroup, workerGroup)
			.channel(NioServerSocketChannel.class);
	}

	@Override
	public void			close()
	{
		Future<?> shutdownWorkerFuture = workerGroup.shutdownGracefully(0, 15, TimeUnit.SECONDS);
		Future<?> shutdownBossFuture = bossGroup.shutdownGracefully(0, 15, TimeUnit.SECONDS);
		shutdownWorkerFuture.awaitUninterruptibly();
		shutdownBossFuture.awaitUninterruptibly();
	}

	@Override
	public synchronized  Executor	getWorkExecutor()
	{
		if (workExecutor == null)
			workExecutor = Executors.newCachedThreadPool();
		return workExecutor;
	}

	public synchronized void	setWorkExecutor(Executor executor)
	{
		this.workExecutor = executor;
	}

	protected EventLoopGroup	bossGroup;

	protected EventLoopGroup	workerGroup;

	protected Executor		workExecutor;
}
