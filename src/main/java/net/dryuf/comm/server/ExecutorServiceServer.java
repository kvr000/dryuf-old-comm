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

package net.dryuf.comm.server;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.common.net.HostAndPort;


public abstract class ExecutorServiceServer extends AbstractSyncServer
{
	public				ExecutorServiceServer()
	{
		super();
	}

	/**
	 * Finishes the process, terminates children.
	 */
	@Override
	public void			finish()
	{
		requestExecutor.shutdownNow();
		for (;;) {
			try {
				requestExecutor.awaitTermination(1000000000, TimeUnit.SECONDS);
				break;
			}
			catch (InterruptedException e) {
			}
		}
	}

	/**
	 * Adds running child.
	 */
	protected void			submitChild(Runnable child)
	{
		requestExecutor.submit(child);
	}

	public void			setExecutorService(ScheduledExecutorService requestExecutor)
	{
		this.requestExecutor = requestExecutor;
	}

	protected ScheduledExecutorService requestExecutor;
}
