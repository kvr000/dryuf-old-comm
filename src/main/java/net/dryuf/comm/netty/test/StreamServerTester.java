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

import net.dryuf.comm.server.netty.AbstractStreamNettyServer;
import io.netty.channel.Channel;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;


public class StreamServerTester<SERVERT extends AbstractStreamNettyServer> extends AbstractServerTester<SERVERT>
{
	public Channel                  startClient()
	{
		try {
			clientStream = new PipedInputStream();
			Channel client = nettyTestManager.getNettyManager().createStreamBootstrap()
				.handler(new StreamingChannelHandler(new PipedOutputStream(clientStream)))
				.connect(server.getLocalAddress())
				.sync()
				.channel();
			nettyTestManager.addManagedChannel(client);
			return client;
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public PipedInputStream         getClientStream()
	{
		return clientStream;
	}

	public DataInputStream          getClientDataStream()
	{
		if (clientDataStream == null)
			clientDataStream = new DataInputStream(getClientStream());
		return clientDataStream;
	}

	protected PipedInputStream      clientStream;

	protected DataInputStream       clientDataStream;
}
