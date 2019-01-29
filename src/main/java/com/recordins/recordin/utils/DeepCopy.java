/*
 * Record'in
 *
 * Copyright (C) 2019 Blockchain Record'in Solutions
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.recordins.recordin.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.*;

/**
 * Utility for making deep copies (vs. clone()'s shallow copies) of
 * objects. Objects are first serialized and then deserialized. Error
 * checking is fairly minimal in this implementation. If an object is
 * encountered that cannot be serialized (or that references an object
 * that cannot be serialized) an error is printed to System.err and
 * null is returned. Depending on your specific application, it might
 * make more sense to have copy(...) re-throw the exception.
 */
public class DeepCopy {

    /* Logger for console output */
    private static final Logger logger = LoggerFactory.getLogger(DeepCopy.class);

    public static Object unserialize(byte[] orig) {
        Object obj = null;
        try {
            FastByteArrayOutputStream fbos = new FastByteArrayOutputStream();
            fbos.write(orig);
            fbos.flush();
            fbos.close();

            ObjectInputStream in =
                    new ObjectInputStream(fbos.getInputStream());
            obj = in.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
        return obj;
    }

    public static byte[] serialize(Object orig) {
        byte[] obj = null;
        try {
            FastByteArrayOutputStream fbos =
                    new FastByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(fbos);
            out.writeObject(orig);

            out.flush();
            out.close();

            InputStream in = fbos.getInputStream();
            obj = new byte[in.available()];
            in.read(obj);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return obj;
    }

    /**
     * Returns a copy of the object, or null if the object cannot
     * be serialized.
     */
    public static Object copy(Object orig) {
        Object obj = null;
        try {
            // Write the object out to a byte array
            FastByteArrayOutputStream fbos =
                    new FastByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(fbos);
            out.writeObject(orig);
            out.flush();
            out.close();

            // Retrieve an input stream from the byte array and read
            // a copy of the object back in.
            ObjectInputStream in =
                    new ObjectInputStream(fbos.getInputStream());
            obj = in.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
        return obj;
    }

    private static class FastByteArrayInputStream extends InputStream {
        /**
         * Our byte buffer
         */
        protected byte[] buf = null;

        /**
         * Number of bytes that we can read from the buffer
         */
        protected int count = 0;

        /**
         * Number of bytes that have been read from the buffer
         */
        protected int pos = 0;

        public FastByteArrayInputStream(byte[] buf, int count) {
            this.buf = buf;
            this.count = count;
        }

        public final int available() {
            return count - pos;
        }

        public final int read() {
            return (pos < count) ? (buf[pos++] & 0xff) : -1;
        }

        public final int read(byte[] b, int off, int len) {
            if (pos >= count)
                return -1;

            if ((pos + len) > count)
                len = (count - pos);

            System.arraycopy(buf, pos, b, off, len);
            pos += len;
            return len;
        }

        public final long skip(long n) {
            if ((pos + n) > count)
                n = count - pos;
            if (n < 0)
                return 0;
            pos += n;
            return n;
        }

    }

    private static class FastByteArrayOutputStream extends OutputStream {
        /**
         * Buffer and size
         */
        protected byte[] buf = null;
        protected int size = 0;

        /**
         * Constructs a stream with buffer capacity size 5K
         */
        public FastByteArrayOutputStream() {
            this(5 * 1024);
        }

        /**
         * Constructs a stream with the given initial size
         */
        public FastByteArrayOutputStream(int initSize) {
            this.size = 0;
            this.buf = new byte[initSize];
        }

        /**
         * Ensures that we have a large enough buffer for the given size.
         */
        private void verifyBufferSize(int sz) {
            if (sz > buf.length) {
                byte[] old = buf;
                buf = new byte[Math.max(sz, 2 * buf.length)];
                System.arraycopy(old, 0, buf, 0, old.length);
                old = null;
            }
        }

        public int getSize() {
            return size;
        }

        /**
         * Returns the byte array containing the written data. Note that this
         * array will almost always be larger than the amount of data actually
         * written.
         */
        public byte[] getByteArray() {
            return buf;
        }

        public final void write(byte b[]) {
            verifyBufferSize(size + b.length);
            System.arraycopy(b, 0, buf, size, b.length);
            size += b.length;
        }

        public final void write(byte b[], int off, int len) {
            verifyBufferSize(size + len);
            System.arraycopy(b, off, buf, size, len);
            size += len;
        }

        public final void write(int b) {
            verifyBufferSize(size + 1);
            buf[size++] = (byte) b;
        }

        public void reset() {
            size = 0;
        }

        /**
         * Returns a ByteArrayInputStream for reading back the written data
         */
        public InputStream getInputStream() {
            return new FastByteArrayInputStream(buf, size);
        }

    }
}
