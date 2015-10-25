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

import com.google.common.net.HostAndPort;

import net.dryuf.core.ByteUtil;
import net.dryuf.core.Dryuf;


public abstract class DatagramServer extends SocketServer
{
	public				DatagramServer()
	{
		super();
	}

	protected DatagramChannel	createListener(SocketAddress listenerBind)
	{
		try {
			DatagramChannel socket = DatagramChannel.open();
			socket.socket().bind(listenerBind);
			logger.debug("created listener on "+listenerBind.toString());
			return socket;
		}
		catch (RuntimeException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void			prepare()
	{
		super.prepare();
		listener = createListener(listenerBind);
	}

	@Override
	public void			finish()
	{
		try {
			listener.close();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void			loop()
	{
		final ByteBuffer packet = ByteBuffer.allocate(serverDatagramHandler.configuredBufferSize());
		for (;;) {
			final SocketAddress peerAddress;
			try {
				peerAddress = listener.receive(packet);
			}
			catch (InterruptedIOException exin) {
				continue;
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
			final byte[] in = new byte[packet.position()];
			System.arraycopy(packet.array(), 0, in, 0, in.length);
			packet.clear();
			submitChild(() -> {
				byte[] out;
				try {
					out = serverDatagramHandler.processDatagram(peerAddress, in);
					if (out != null && out.length != 0) {
						try {
							listener.send(ByteBuffer.wrap(out), peerAddress);
						}
						catch (SocketException ex) {
							throw new RuntimeException(ex);
						}
						catch (IOException ex) {
							throw new RuntimeException(ex);
						}
					}
				}
				catch (Exception ex) {
					logger.error("failed to process packet from "+peerAddress.toString()+": "+Dryuf.formatExceptionFull(ex)+"\t"+ByteUtil.dumpBytes(packet.array()));
				}
			});
		}
	}

	public void			setServerDatagramHandler(ServerDatagramHandler serverDatagramHandler)
	{
		this.serverDatagramHandler = serverDatagramHandler;
	}

	protected DatagramChannel	listener;

	protected ServerDatagramHandler	serverDatagramHandler;
}
