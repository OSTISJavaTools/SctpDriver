package net.ostis.common.sctpclient.transport;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import net.ostis.common.sctpclient.constants.ScParameterSize;
import net.ostis.common.sctpclient.model.ScParameter;
import net.ostis.common.sctpclient.model.request.SctpRequest;
import net.ostis.common.sctpclient.model.request.SctpRequestBody;
import net.ostis.common.sctpclient.model.request.SctpRequestHeader;

class SctpRequestBytesBuilder {

    public static byte[] build(SctpRequest request) {

        byte[] bodyByteArray = parseRequestBody(request.getBody());
        byte[] headerByteArray = parseRequestHeader(request.getHeader());
        ByteBuffer resultBuffer = ByteBuffer
                .allocate(bodyByteArray.length + headerByteArray.length);
        resultBuffer.put(headerByteArray);
        resultBuffer.put(bodyByteArray);
        return resultBuffer.array();
    }

    private static byte[] parseRequestHeader(SctpRequestHeader requestHeader) {

        ByteBuffer tempBuffer = ByteBuffer.allocate(ScParameterSize.SC_HEADER.getSize());
        tempBuffer.order(ByteOrder.LITTLE_ENDIAN);
        tempBuffer.put(requestHeader.getCommandType().getValue());
        tempBuffer.put(requestHeader.getFlag());
        tempBuffer.putInt(requestHeader.getCommandId());
        tempBuffer.putInt(requestHeader.getArgumentSize());
        return tempBuffer.array();
    }

    private static byte[] parseRequestBody(SctpRequestBody requestBody) {

        ByteBuffer tempBuffer = ByteBuffer.allocate(requestBody.getByteLenght());
        List<ScParameter> parameterList = requestBody.getParameterList();
        for (ScParameter parameter : parameterList) {
            tempBuffer.put(parameter.getBytes());
        }
        return tempBuffer.array();
    }
}
