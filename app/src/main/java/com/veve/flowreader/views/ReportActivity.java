package com.veve.flowreader.views;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfDocument;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.veve.flowreader.BuildConfig;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class ReportActivity extends AppCompatActivity {

    private static byte[] originalImage;
    private static byte[] reflowedImage;
    private static byte[] glyphs;
    private static final String BOUNDARY = "---------------------------104659796718797242641237073228";
    private static final String BOUNDARY_PART = "--";
    private static long totalSize = 0L;
    private static ProgressBar progressBar;
    private static long reportId;
    private static long bookId;
    private static int position;

    Intent backToPageIntent;
    TextView progressLabel;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        setContentView(R.layout.activity_report);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressBar = findViewById(R.id.progress);
        progressBar.setMax(100);
        progressLabel = findViewById(R.id.progress_label);

        backToPageIntent = new Intent(ReportActivity.this, PageActivity.class);
        backToPageIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        backToPageIntent.putExtra(Constants.BOOK_ID, bookId);
        backToPageIntent.putExtra(Constants.POSITION, position);

        long bookId = getIntent().getLongExtra("reportId", -1);

        ReportGetterTask reportGetterTask = new ReportGetterTask();

        try {
            reportGetterTask.execute(bookId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        findViewById(R.id.fab).setOnClickListener((view) -> {
                requestPermissions();
                ReportSenderTask reportSenderTask = new ReportSenderTask();
                reportSenderTask.execute();
            }
        );

        findViewById(R.id.back).setOnClickListener((view) -> {
                AppDatabase appDatabase = AppDatabase.getInstance(getApplicationContext());
                appDatabase.daoAccess().deleteReport(reportId);
                ReportActivity.this.startActivity(backToPageIntent);
            }
        );

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
            cursor.moveToFirst();
            bookId = cursor.getLong(cursor.getColumnIndex("bookId"));
            position = cursor.getInt(cursor.getColumnIndex("position"));
            reportId = cursor.getLong(cursor.getColumnIndex("id"));
            try {
                ByteArrayOutputStream originalImageOs = new ByteArrayOutputStream();
                FileInputStream originalImageInputStream =
                        new FileInputStream(new String(cursor.getBlob(cursor.getColumnIndex("originalPage"))));
                byte[] buffer = new byte[100];
                while (originalImageInputStream.read(buffer) != -1) {
                    originalImageOs.write(buffer);
                }
                originalImageOs.close();
                originalImage = originalImageOs.toByteArray();
                originalImageInputStream.close();

                ByteArrayOutputStream reflowedImageOs = new ByteArrayOutputStream();
                FileInputStream reflowedImageInputStream =
                        new FileInputStream(new String(cursor.getBlob(cursor.getColumnIndex("overturnedPage"))));
                while (reflowedImageInputStream.read(buffer) != -1) {
                    reflowedImageOs.write(buffer);
                }
                reflowedImageOs.close();
                reflowedImage = reflowedImageOs.toByteArray();
                reflowedImageInputStream.close();

            } catch (Exception e) {
                Log.e(getClass().getName(), e.getMessage(), e);
            }

            glyphs = cursor.getBlob(cursor.getColumnIndex("glyphs"));
            totalSize += reflowedImage.length;
            totalSize += originalImage.length;
            totalSize += glyphs.length;
            progressLabel.setText(String.format(Locale.getDefault(),
                    getString(R.string.notify_send_size), totalSize));
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            handler.post(() -> {
                Log.v(getClass().getName(), "Progress " + values);
            });
        }
    }

    class ReportSenderTask extends AsyncTask<Void, Float, Long> {

        Long reportIncomingId;

        @Override
        protected Long doInBackground(Void... voids) {
            publishProgress(0F);
            try {
                URL url = new URL(Constants.REPORT_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
                conn.setDoOutput(true);
                conn.connect();
                OutputStream os = conn.getOutputStream();
                handler.post(() -> {progressLabel.setText(R.string.sending_original);});
                os.write(getFileData(originalImage, "image/jpeg", "originalImage", "originalImage.jpeg", false));
                publishProgress(((float)originalImage.length/(float)totalSize));
                Thread.sleep(1000);
                handler.post(() -> {progressLabel.setText(R.string.sending_reflowed);});
                os.write(getFileData(reflowedImage, "image/jpeg", "overturnedImage", "overturnedImage.jpeg", false));
                publishProgress(((float)(originalImage.length+reflowedImage.length)/(float)totalSize));
                Thread.sleep(1000);
                handler.post(() -> {progressLabel.setText(R.string.sending_glyphs);});
                os.write(getFileData(glyphs, "application/json", "glyphs", "glyphs.json", false));
                publishProgress(((float)(originalImage.length+reflowedImage.length + glyphs.length)/(float)totalSize));
                Thread.sleep(1000);
                os.write(getTextData(BuildConfig.GitHash, "version", true));
                os.flush();
                handler.post(() -> {progressLabel.setText(R.string.report_response);});
                Log.v(getClass().getName(), "HIROKU_RESPONSE response code" + conn.getResponseCode());
                InputStream is = conn.getInputStream();
                byte[] buffer = new byte[100];
                int available = is.available();
                int counter = 0;
                publishProgress(0F);
                while (is.read(buffer) != -1) {
//                    publishProgress((float)((counter + 100)/available));
                    Log.v(getClass().getName(), "HIROKU_RESPONSE" + new String(buffer));
                }
                handler.post(() -> {progressLabel.setText(R.string.report_sent);});
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            AppDatabase appDatabase = AppDatabase.getInstance(getApplicationContext());
            appDatabase.daoAccess().deleteReport(reportId);
            startActivity(backToPageIntent);
        }

        @Override
        protected void onProgressUpdate(Float... values) {
            super.onProgressUpdate(values);
            handler.post(() -> {
                Log.v(getClass().getName(), "Progress send" + values[0]);
                if (values != null && values.length > 0) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress((int) (values[0] * 100));
                }
            });
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

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_NETWORK_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_NETWORK_STATE)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.ACCESS_NETWORK_STATE,
                                Manifest.permission.INTERNET
                        }, 1);
            }
        }
    }


}




