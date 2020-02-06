package com.veve.flowreader;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.text.MessageFormat;

import static android.content.ContentResolver.SCHEME_CONTENT;
import static android.content.ContentResolver.SCHEME_FILE;
import static android.drm.DrmStore.DrmObjectType.CONTENT;

public interface BookContentResolver {

    static final MessageFormat fileInputFormat = new MessageFormat("file://{0}");

    static final MessageFormat contentInputFormat = new MessageFormat("{0}_files{1}");

    static final MessageFormat outputFormat = new MessageFormat("/storage/emulated/0{0}");

    public static File contentToFile(Context context, Uri uri) throws Exception {
        String path = null;
        try {
            if (uri.getScheme().equals(SCHEME_CONTENT)) {
                String inputStr = uri.getEncodedPath();
                String filePath = null;
                path = outputFormat.format(new String[]{(String) (contentInputFormat.parse(inputStr)[1])});
            } else if (uri.getScheme().equals(SCHEME_FILE)) {
                String inputStr = uri.toString();
                path = (String)fileInputFormat.parse(inputStr)[0];
            }
        } catch (Exception e) {
            Log.v(BookContentResolver.class.getName(), "Opening " + uri.toString());
            InputStream is = context.getContentResolver().openInputStream(uri);

            String fileName = URLDecoder.decode(uri.toString(), "UTF-8");
            fileName = fileName.substring(1 + fileName.lastIndexOf('/'));
            File file = new File(context.getExternalFilesDir(null), fileName);
            OutputStream os = new FileOutputStream(file);
            byte[] buffer = new byte[100];
            while(is.read(buffer) != -1) {
                os.write(buffer);
            }
            os.close();
            is.close();
            //path = file.getPath();
            //String p = URLDecoder.decode(path, "UTF-8");
            //File f = new File(p);

            //boolean renamed = file.renameTo(f);
            //if (renamed) {
            //    return f;
            //}
            return file;

        }

        return new File(URLDecoder.decode(path, "UTF-8"));
    }

}

//content://ru.yandex.disk.filescache/d0/BOOKS/KVANT/04_Opyty_Domashney_Laboratorii.djvu