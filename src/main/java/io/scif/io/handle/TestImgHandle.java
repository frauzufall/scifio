/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2017 SCIFIO developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package io.scif.io.handle;

import io.scif.io.location.TestImgLocation;

import java.io.IOException;

import org.scijava.io.handle.AbstractDataHandle;
import org.scijava.io.handle.DataHandle;
import org.scijava.plugin.Plugin;

/**
 * Handle for {@link TestImgLocation}, does not provide any data Access
 * 
 * @author Gabriel Einsdorf
 */
@Plugin(type = DataHandle.class)
public class TestImgHandle extends AbstractDataHandle<TestImgLocation> {

	@Override
	public boolean isReadable() {
		return false;
	}

	@Override
	public boolean isWritable() {
		return false;
	}

	@Override
	public boolean exists() throws IOException {
		throw fail();
	}

	@Override
	public long offset() throws IOException {
		throw fail();
	}

	@Override
	public void seek(long pos) throws IOException {
		throw fail();

	}

	@Override
	public long length() throws IOException {
		throw fail();
	}

	@Override
	public void setLength(long length) throws IOException {
		throw fail();
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		throw fail();
	}

	@Override
	public Class<TestImgLocation> getType() {
		return TestImgLocation.class;
	}

	@Override
	public byte readByte() throws IOException {
		throw fail();
	}

	@Override
	public void write(int b) throws IOException {
		throw fail();
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		throw fail();
	}

	private IOException fail() {
		return new IOException(
			"TestImgHandle does not provide access to it's data!");
	}

	@Override
	public void close() throws IOException {
		// NO-OP
	}

}
