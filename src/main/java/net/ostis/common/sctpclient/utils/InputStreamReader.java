package net.ostis.common.sctpclient.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class InputStreamReader {

    public static final int INTEGER_BYTE_SIZE = 4;

    public static int readInt(InputStream source) throws IOException {

        byte[] intBytes = new byte[INTEGER_BYTE_SIZE];
        source.read(intBytes);
        ByteBuffer tempBuffer = ByteBuffer.wrap(intBytes);
        tempBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return tempBuffer.getInt();
    }

}
