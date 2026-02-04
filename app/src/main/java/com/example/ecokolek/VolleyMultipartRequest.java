package com.example.ecokolek;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

public class VolleyMultipartRequest extends Request<NetworkResponse> {

    private final Response.Listener<NetworkResponse> mListener;
    private final Map<String, String> mParams;
    private final Map<String, DataPart> mByteData; // key -> DataPart

    public VolleyMultipartRequest(int method, String url,
                                  Response.Listener<NetworkResponse> listener,
                                  Response.ErrorListener errorListener,
                                  Map<String, String> params,
                                  Map<String, DataPart> byteData) {
        super(method, url, errorListener);
        this.mListener = listener;
        this.mParams = params;
        this.mByteData = byteData;
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data;boundary=" + boundary;
    }

    private final String boundary = "apiclient-" + System.currentTimeMillis();
    private final String twoHyphens = "--";
    private final String lineEnd = "\r\n";

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            // text params
            if (mParams != null) {
                for (Map.Entry<String, String> entry : mParams.entrySet()) {
                    bos.write((twoHyphens + boundary + lineEnd).getBytes());
                    bos.write(("Content-Disposition: form-data; name=\"" +
                            entry.getKey() + "\"" + lineEnd).getBytes());
                    bos.write(("Content-Type: text/plain; charset=UTF-8" + lineEnd).getBytes());
                    bos.write(lineEnd.getBytes());
                    bos.write(entry.getValue().getBytes("UTF-8"));
                    bos.write(lineEnd.getBytes());
                }
            }

            // file params
            if (mByteData != null) {
                for (Map.Entry<String, DataPart> entry : mByteData.entrySet()) {
                    DataPart dp = entry.getValue();
                    bos.write((twoHyphens + boundary + lineEnd).getBytes());
                    bos.write(("Content-Disposition: form-data; name=\"" +
                            entry.getKey() + "\"; filename=\"" +
                            dp.getFileName() + "\"" + lineEnd).getBytes());
                    bos.write(("Content-Type: " + dp.getType() + lineEnd).getBytes());
                    bos.write(lineEnd.getBytes());
                    bos.write(dp.getContent());
                    bos.write(lineEnd.getBytes());
                }
            }

            bos.write((twoHyphens + boundary + twoHyphens + lineEnd).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bos.toByteArray();
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        return Response.success(response, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        mListener.onResponse(response);
    }

    public static class DataPart {
        private final String fileName;
        private final byte[] content;
        private final String type;

        public DataPart(String fileName, byte[] content, String type) {
            this.fileName = fileName;
            this.content = content;
            this.type = type;
        }

        public String getFileName() { return fileName; }
        public byte[] getContent() { return content; }
        public String getType() { return type; }
    }
}

