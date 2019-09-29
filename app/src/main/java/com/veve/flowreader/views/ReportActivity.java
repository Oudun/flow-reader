package com.veve.flowreader.views;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.TextView;

import com.veve.flowreader.Constants;
import com.veve.flowreader.R;
import com.veve.flowreader.dao.AppDatabase;
import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.dao.DaoAccess;
import com.veve.flowreader.dao.ReportRecord;
import com.veve.flowreader.model.BooksCollection;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageRenderer;
import com.veve.flowreader.model.PageRendererFactory;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class ReportActivity extends AppCompatActivity {

    private static byte[] originalImage;
    private static byte[] overturnedImage;
    private static byte[] glyphs;
    private static final String BOUNDARY = "---------------------------104659796718797242641237073228";
    private static final String BOUNDARY_PART = "--";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        long bookId = getIntent().getLongExtra("reportId", -1);


        ReportGetterTask reportGetterTask = new ReportGetterTask();

        try {
            reportGetterTask.execute(bookId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ReportSenderTask reportSenderTask = new ReportSenderTask();
            }
        });

    }

    class ReportGetterTask extends AsyncTask<Long, Void, Cursor> {

        @Override
        protected Cursor doInBackground(Long... longs) {
            AppDatabase appDatabase = AppDatabase.getInstance(getApplicationContext());
            DaoAccess daoAccess = appDatabase.daoAccess();
            return daoAccess.getReport(longs[0]);
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);
            long totalSize = 0L;
            cursor.moveToFirst();
            originalImage = cursor.getBlob(cursor.getColumnIndex("originalPage"));
            overturnedImage = cursor.getBlob(cursor.getColumnIndex("overturnedPage"));
            glyphs = cursor.getBlob(cursor.getColumnIndex("glyphs"));
            totalSize += overturnedImage.length;
            totalSize += originalImage.length;
            totalSize += glyphs.length;
            TextView textView = findViewById(R.id.size_note);
            textView.setText(String.format(Locale.getDefault(),
                    getString(R.string.notify_send_size), totalSize));
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            Log.v(getClass().getName(), "Progress " + values);
        }
    }

    static class ReportSenderTask extends AsyncTask<ReportRecord, Long, Long> {

        Long reportIncomingId;

        @Override
        protected Long doInBackground(ReportRecord... reportRecords) {
//            publishProgress();
            try {
                URL url = new URL(Constants.REPORT_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
                conn.setDoOutput(true);
                conn.connect();
                OutputStream os = conn.getOutputStream();
                //os.write(getTextData("text", "fieldName", true));
                os.write(getFileData(originalImage, "image/jpeg", "originalImage", "originalImage.jpeg", true));
                //os.write(data);
                os.flush();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                return null;

            }

        }

        @Override
        protected void onProgressUpdate(Long... values) {
            super.onProgressUpdate(values);
        }

        private byte[] getTextData(String text, String fieldName, boolean lastOne) throws Exception {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(BOUNDARY_PART.getBytes());
            byteArrayOutputStream.write(BOUNDARY.getBytes());
            byteArrayOutputStream.write("\r\n".getBytes());
            byteArrayOutputStream.write("Content-Disposition: form-data; ".getBytes());
            byteArrayOutputStream.write(("name=\"" + fieldName + "\"").getBytes());
            byteArrayOutputStream.write("\r\n".getBytes());
            byteArrayOutputStream.write("\r\n".getBytes());
            byteArrayOutputStream.write(text.getBytes());
            byteArrayOutputStream.write("\r\n".getBytes());
            byteArrayOutputStream.write(BOUNDARY_PART.getBytes());
            byteArrayOutputStream.write(BOUNDARY.getBytes());
            if (lastOne)
                byteArrayOutputStream.write(BOUNDARY_PART.getBytes());
            byteArrayOutputStream.write("\r\n".getBytes());
            return byteArrayOutputStream.toByteArray();
        }

        private byte[] getFileData(byte[] data, String contentType, String fieldName, String fileName, boolean lastOne) throws Exception {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(BOUNDARY_PART.getBytes());
            byteArrayOutputStream.write(BOUNDARY.getBytes());
            byteArrayOutputStream.write("\r\n".getBytes());
            byteArrayOutputStream.write(("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"").getBytes());
            byteArrayOutputStream.write("\r\n".getBytes());
            byteArrayOutputStream.write(("Content-Type: " + contentType).getBytes());
            byteArrayOutputStream.write("\r\n".getBytes());
            byteArrayOutputStream.write("\r\n".getBytes());
            byteArrayOutputStream.write(data);
            byteArrayOutputStream.write("\r\n".getBytes());
            byteArrayOutputStream.write(BOUNDARY_PART.getBytes());
            byteArrayOutputStream.write(BOUNDARY.getBytes());
            if (lastOne)
                byteArrayOutputStream.write(BOUNDARY_PART.getBytes());
            byteArrayOutputStream.write("\r\n".getBytes());
            return byteArrayOutputStream.toByteArray();
        }

    }


}




