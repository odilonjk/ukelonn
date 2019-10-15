/*
 * Copyright 2016-2018 Steinar Bang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations
 * under the License.
 */
package no.priv.bang.ukelonn.mocks;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A handwritten mock {@link OutputStream}.
 *
 * This is an {@link OutputStream} that will throw {@link IOException} on
 * all methods declaring {@link IOException}.
 *
 * @author Steinar Bang
 *
 */
public class MockOutputStreamThatThrowsIOExceptionOnEverything extends OutputStream {

    @Override
    public void write(int b) throws IOException {
        throw new IOException();
    }

    @Override
    public void close() throws IOException {
        throw new IOException();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        throw new IOException();
    }

    @Override
    public void write(byte[] b) throws IOException {
        throw new IOException();
    }

}
