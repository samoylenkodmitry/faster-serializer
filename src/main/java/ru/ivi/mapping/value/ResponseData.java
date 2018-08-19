package ru.ivi.mapping.value;

import java.io.IOException;
import java.io.InputStream;

import ru.ivi.utils.IoUtils;

public final class ResponseData {

    private final byte[] mData;
    private final String mUrl;
    private final String mErrMsg;
    
    public ResponseData(final InputStream inputStream, final String url) throws IOException {
        mData = IoUtils.readBytes(inputStream, true);
        mUrl = url;
        mErrMsg = null;
    }
    
    public ResponseData(final String errMsg, final String url) throws IOException {
        mData = null;
        mUrl = url;
        mErrMsg = errMsg;
    }
    
    public String getErrMsg() {
        return mErrMsg;
    }

    public byte[] getData() {
        return mData;
    }

    public String getUrl() {
        return mUrl;
    }

    @Override
    public String toString() {
        return mUrl + " result: " + (mData == null ? mErrMsg : mData.length + " bytes");
    }
}