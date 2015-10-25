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
import net.dryuf.comm.server.netty.AbstractSocketNettyServer;
import net.dryuf.core.AppContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.concurrent.ListenableFuture;

import javax.inject.Inject;
import java.net.InetSocketAddress;


/**
 * Created by rat on 08/10/2015.
 */
public class AbstractServerTester<SERVERT extends AbstractSocketNettyServer> extends Object implements AutoCloseable
{
	public NettyManager             getNettyManager()
	{
		return nettyManager;
	}

	public NettyTestManager         getNettyTestManager()
	{
		return nettyTestManager;
	}

	@Override
	public void                     close()
	{
		terminateServer();
		try {
			serverTerminateFuture.get();
		}
		catch (Exception e) {
			logger.error("Failed to terminate server.", e);
		}
		nettyTestManager.close();
	}

	public SERVERT                   startServer(Class<SERVERT> serverClass)
	{
		server = appContainer.createBeaned(serverClass, null);
		server.setListenerBind(new InetSocketAddress("localhost", 0));
		serverTerminateFuture = server.start();
		return server;
	}

	protected void                  terminateServer()
	{
		server.terminate();
	}

	protected SERVERT               server;

	protected ListenableFuture<?>	serverTerminateFuture;

	protected NettyTestManager      nettyTestManager = new NettyTestManager();

	protected NettyManager          nettyManager = nettyTestManager.getNettyManager();

	@Inject
	protected AppContainer          appContainer;

	protected Logger		logger = LogManager.getLogger(getClass());
}
