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
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.TimeUnit;

import com.google.common.net.HostAndPort;

import org.apache.commons.lang3.Validate;


public abstract class StreamServer extends SocketServer
{
	public				StreamServer()
	{
		super();
	}

	public AsynchronousServerSocketChannel createListener(SocketAddress listenerBind)
	{
		final AsynchronousServerSocketChannel socket;
		try {
			socket = AsynchronousServerSocketChannel.open();
			socket.bind(listenerBind);
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
		}
		finally {
			super.finish();
		}
	}

	public void			loop()
	{
		Validate.notNull(serverListenerHandler);
		listener.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
			@Override
			public void completed(AsynchronousSocketChannel result, Object attachment) {
				listener.accept(null, this);
				startClient(result);
			}

			@Override
			public void failed(Throwable exc, Object attachment) {
				failedAccept(exc);
			}
		});
		for (;;) {
			try {
				Thread.sleep(86400000);
			}
			catch (InterruptedException e) {
				break;
			}
		}
	}

	private void			startClient(AsynchronousSocketChannel client)
	{
		try {
			SocketAddress peerAddress;
			try {
				peerAddress = client.getRemoteAddress();
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
			ServerStreamHandler streamHandler = serverListenerHandler.accepted(peerAddress);
			final ByteBuffer buffer = ByteBuffer.allocate(streamHandler.configuredBufferSize());

			client.read(buffer, 60000, TimeUnit.MILLISECONDS, buffer, new CompletionHandler<Integer, ByteBuffer>()
			{
				@Override
				public void completed(Integer result, ByteBuffer buffer)
				{
					try {
						if (result < 0) {
							try {
								streamHandler.close();
							}
							finally {
								closeSocket();
							}
							return;
						}

						try {
							byte[] out = streamHandler.processInput(buffer.array(), 0, buffer.position());
							if (out != null) {
								ByteBuffer outBuffer = ByteBuffer.wrap(out);
								client.write(outBuffer, 60000, TimeUnit.MILLISECONDS, outBuffer, new CompletionHandler<Integer, ByteBuffer>()
								{
									@Override
									public void completed(Integer result, ByteBuffer writeBuffer)
									{
										if (outBuffer.remaining() > 0) {
											client.write(outBuffer, 60000, TimeUnit.MILLISECONDS, outBuffer, this);
										}
										else {
											restartRead();
										}
									}

									@Override
									public void failed(Throwable exc, ByteBuffer writeBuffer)
									{
										failedSocket(exc);
									}
								});
							}
							else {
								restartRead();
							}
						}
						catch (RuntimeException ex) {
							try {
								streamHandler.close();
							}
							finally {
								closeSocket();
							}
						}
					}
					catch (RuntimeException ex) {
						failed(ex, buffer);
					}
				}

				@Override
				public void failed(Throwable exc, ByteBuffer buffer)
				{
					failedSocket(exc);
				}

				public void failedSocket(Throwable ex)
				{
					try {
						failedClient(streamHandler, ex);
					}
					finally {
						closeSocket();
					}
				}

				public void closeSocket()
				{
					try {
						client.close();
					}
					catch (IOException e) {
						throw new RuntimeException(e);
					}
				}

				public void restartRead()
				{
					int terminating = streamHandler.getTerminating();
					if (terminating != 0) {
						try {
							streamHandler.close();
						}
						finally {
							closeSocket();
						}
					}
					else {
						client.read(buffer, 600000, TimeUnit.MILLISECONDS, buffer, this);
					}
				}
			});
			try {
				System.out.println("started read on client "+client.getRemoteAddress().toString());
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		catch (RuntimeException ex) {
			try {
				client.close();
			}
			catch (IOException e) {
			}
			throw ex;
		}
	}

	protected void			failedAccept(Throwable ex)
	{
		logger.fatal(ex);
		this.runThread.interrupt();
	}

	protected void			failedClient(ServerStreamHandler client, Throwable ex)
	{
		logger.error(ex);
	}

	public void			setServerListenerHandler(ServerListenerHandler serverListenerHandler)
	{
		this.serverListenerHandler = serverListenerHandler;
	}

	protected AsynchronousServerSocketChannel listener;

	protected ServerListenerHandler	serverListenerHandler;
}
