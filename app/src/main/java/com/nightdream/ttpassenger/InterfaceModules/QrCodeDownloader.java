package com.nightdream.ttpassenger.InterfaceModules;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class QrCodeDownloader extends AsyncTask<String, Void, Bitmap> {
    private final WeakReference<ImageView> imageViewWeakReference;

    public QrCodeDownloader (ImageView imageView){
        imageViewWeakReference = new WeakReference<>(imageView);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap){
        if (isCancelled()){
            bitmap = null;
        }
        ImageView imageView = imageViewWeakReference.get();
        if (imageView != null){
            if (bitmap != null){
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    @Override
    protected Bitmap doInBackground(String... strings) {
        return downloadBitmap(strings[0]);
    }

    private Bitmap downloadBitmap(String string) {
        HttpURLConnection urlConnection = null;
        try{
            URL uri = new URL(string);
            urlConnection = (HttpURLConnection) uri.openConnection();
            int status = urlConnection.getResponseCode();
            if(status != HttpURLConnection.HTTP_OK ){
                return null;
            }
            InputStream inputStream = urlConnection.getInputStream();
            if (inputStream != null){
                return BitmapFactory.decodeStream(inputStream);
            }
        }catch (Exception e){
                urlConnection.disconnect();
        }finally {
            if (urlConnection != null){
                urlConnection.disconnect();
            }
        }
        return null;
    }

}
