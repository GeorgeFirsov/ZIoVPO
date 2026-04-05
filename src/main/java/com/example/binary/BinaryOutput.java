package com.example.binary;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class BinaryOutput {

    private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private final DataOutputStream outputStream = new DataOutputStream(byteArrayOutputStream);

    public void writeMagic(byte[] value) {
        if (value == null || value.length != 4) {
            throw new IllegalArgumentException("magic must contain exactly 4 bytes");
        }
        writeBytes(value);
    }

    public void writeU8(int value) {
        if (value < 0 || value > 0xFF) {
            throw new IllegalArgumentException("uint8 out of range");
        }
        try {
            outputStream.writeByte(value);
        } catch (IOException e) {
            throw new IllegalStateException("cannot write uint8", e);
        }
    }

    public void writeU16(int value) {
        if (value < 0 || value > 0xFFFF) {
            throw new IllegalArgumentException("uint16 out of range");
        }
        try {
            outputStream.writeShort(value);
        } catch (IOException e) {
            throw new IllegalStateException("cannot write uint16", e);
        }
    }

    public void writeU32(long value) {
        if (value < 0 || value > 0xFFFFFFFFL) {
            throw new IllegalArgumentException("uint32 out of range");
        }
        try {
            outputStream.writeInt((int) value);
        } catch (IOException e) {
            throw new IllegalStateException("cannot write uint32", e);
        }
    }

    public void writeU64(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("uint64 must be non-negative");
        }
        writeI64(value);
    }

    public void writeI64(long value) {
        try {
            outputStream.writeLong(value);
        } catch (IOException e) {
            throw new IllegalStateException("cannot write int64", e);
        }
    }

    public void writeUuid(UUID value) {
        writeI64(value.getMostSignificantBits());
        writeI64(value.getLeastSignificantBits());
    }

    public void writeUtf8(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        writeU32(bytes.length);
        writeBytes(bytes);
    }

    public void writeLengthPrefixedBytes(byte[] value) {
        writeU32(value.length);
        writeBytes(value);
    }

    public void writeRawBytes(byte[] value) {
        writeBytes(value);
    }

    public byte[] toByteArray() {
        return byteArrayOutputStream.toByteArray();
    }

    private void writeBytes(byte[] value) {
        try {
            outputStream.write(value);
        } catch (IOException e) {
            throw new IllegalStateException("cannot write bytes", e);
        }
    }
}