package com.telenav.osv.http;

import com.android.volley.AuthFailureError;
import com.android.volley.VolleyLog;
import com.telenav.osv.item.KVFile;
import com.telenav.osv.listener.network.GenericResponseListener;
import com.telenav.osv.utils.Log;
import com.telenav.osv.utils.Utils;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Created by Kalman on 10/6/2015.
 */
public class IssueUploadRequest<T> extends KvVolleyStringRequestJarvisAuthorization {

    private static final String FILE_PART_NAME = "file";

    private static final String TAG = "IssueUploadRequest";

    private static final String PARAM_TOKEN = "access_token";

    private static final String PARAM_ISSUE_ID = "issueId";

    private static final String PARAM_ISSUE_INDEX = "fileIndex";

    private static final String PARAM_FILE_TYPE = "fileType";

    private final int mId;

    private final int mIndex;

    private final GenericResponseListener mListener;

    private final KVFile mFile;

    private final String mToken;

    protected Map<String, String> headers;

    private MultipartEntityBuilder mBuilder = MultipartEntityBuilder.create();

    private ProgressiveEntity mProgressiveEntity;

    public IssueUploadRequest(String url, GenericResponseListener listener, String token, KVFile file, int issueId, int index, boolean isJarvisAuthorization, String jarvisAccessToken) {
        super(Method.POST, url, listener, listener, isJarvisAuthorization, jarvisAccessToken);

        mListener = listener;
        mFile = file;
        mId = issueId;
        mIndex = index;

        mToken = token;
        buildMultipartEntity();
    }

    @Override
    public String getBodyContentType() {
        return mProgressiveEntity.getContentType().getValue();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            mProgressiveEntity.writeTo(bos);
        } catch (IOException e) {
            VolleyLog.e("IOException writing to ByteArrayOutputStream bos, building the multipart request.");
        }

        return bos.toByteArray();
    }

    @Override
    public HttpEntity getMultipartEntity() {
        return mProgressiveEntity;
    }

    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }

    private void buildMultipartEntity() {
        mBuilder.addTextBody(PARAM_TOKEN, mToken);
        mBuilder.addTextBody(PARAM_ISSUE_ID, "" + mId);
        mBuilder.addTextBody(PARAM_ISSUE_INDEX, "" + mIndex);
        mBuilder.addTextBody(PARAM_FILE_TYPE, "text");
        if (!mFile.exists()) {
            Log.d(TAG, "buildMultipartEntity: file doesn't exist");
        }
        mBuilder.addBinaryBody(FILE_PART_NAME, mFile, ContentType.create("text/plain"), mFile.getName());
        mBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        mBuilder.setLaxMode().setBoundary("xx").setCharset(Charset.forName("UTF-8"));
        HttpEntity mEntity = mBuilder.build();
        mProgressiveEntity = new ProgressiveEntity(mEntity, null, Utils.fileSize(mFile), null);
    }
}