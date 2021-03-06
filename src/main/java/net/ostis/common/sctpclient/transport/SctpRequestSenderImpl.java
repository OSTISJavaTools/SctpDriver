package net.ostis.common.sctpclient.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import net.ostis.common.sctpclient.exception.ErrorMessage;
import net.ostis.common.sctpclient.exception.InitializationException;
import net.ostis.common.sctpclient.exception.SctpClientException;
import net.ostis.common.sctpclient.exception.ShutdownException;
import net.ostis.common.sctpclient.exception.TransportException;
import net.ostis.common.sctpclient.model.request.SctpRequest;
import net.ostis.common.sctpclient.model.response.SctpResponse;

public class SctpRequestSenderImpl implements SctpRequestSender {

	private InputStream inputStream;

	private OutputStream outputStream;

	private Socket socket;

	public SctpRequestSenderImpl() {

		super();
	}

	@Override
	public void init(String host, int port) throws InitializationException {

		try {
			socket = new Socket(host, port);
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();
		} catch (IOException e) {
			throw new InitializationException(ErrorMessage.TRANSPORT_INIT_ERROR);
		}
	}

	@Override
	public void shutdown() throws ShutdownException {

		try {
			closeResources();
		} catch (IOException e) {
			throw new ShutdownException(ErrorMessage.SHUTDOWN_ERROR);
		}
	}

	private void closeResources() throws IOException {

		socket.close();
		inputStream.close();
		outputStream.close();
	}

	@Override
	public <Type> SctpResponse<Type> sendRequest(SctpRequest request)
			throws SctpClientException {

		try {
			byte[] data = SctpRequestBytesBuilder.build(request);
			outputStream.write(data);
//			outputStream.flush();
			SctpResponseBuilder<Type> responseBuilder = new BytesSctpResponseBuilder<Type>();
			SctpResponse<Type> sctpResponse = responseBuilder.build(
					inputStream, request);
			return sctpResponse;
		} catch (IOException e) {
			throw new TransportException(ErrorMessage.REQUEST_SEND_ERROR);
		}
		
	}

}
