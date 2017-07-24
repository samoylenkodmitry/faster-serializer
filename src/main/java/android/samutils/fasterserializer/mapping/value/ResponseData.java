package android.samutils.fasterserializer.mapping.value;

import android.samutils.utils.IoUtils;

import java.io.IOException;
import java.io.InputStream;

public final class ResponseData {

    private final byte[] mData;
    private final String mUrl;

    public ResponseData(final InputStream inputStream, final String url) throws IOException {
        mData = IoUtils.readBytes(inputStream, true);
        mUrl = url;
    }

    public byte[] getData() {
        return mData;
    }

    public String getUrl() {
        return mUrl;
    }

    @Override
    public String toString() {
        return mUrl + " data: " + mData;
    }
}