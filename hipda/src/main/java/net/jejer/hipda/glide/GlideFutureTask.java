package net.jejer.hipda.glide;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.Target;

import net.jejer.hipda.cache.ImageContainer;
import net.jejer.hipda.ui.ThreadDetailFragment;
import net.jejer.hipda.utils.Logger;

import java.io.File;

/**
 * Load image and get information
 * Created by GreenSkinMonster on 2015-04-24.
 */
public class GlideFutureTask extends AsyncTask<Void, Void, ImageReadyInfo> {

    private Context mCtx;
    private String mUrl;

    public GlideFutureTask(Context context, String url) {
        mCtx = context;
        mUrl = url;
    }

    @Override
    protected ImageReadyInfo doInBackground(Void... voids) {
        try {
            FutureTarget<File> future =
                    Glide.with(mCtx)
                            .load(mUrl)
                            .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
            File cacheFile = future.get();
            Glide.clear(future);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            //Returns null, sizes are in the options variable
            BitmapFactory.decodeFile(cacheFile.getPath(), options);
            int width = options.outWidth;
            int height = options.outHeight;
            String mime = options.outMimeType;

            //calculate display size for image

            //leave 12dp on both left and right side, this should match layout setup
            int maxViewWidth = ThreadDetailFragment.MAX_VIEW_WIDTH - dpToPx(12 * 2);

            //if image width < half maxViewWidth, scale it up for better view
            int maxScaleWidth = Math.round(maxViewWidth * 0.5f);

            double scaleRate = getScaleRate(width);
            int scaledWidth = Math.round((int) (width * scaleRate));
            int scaledHeight = Math.round((int) (height * scaleRate));

            int displayWidth;
            int displayHeight;
            if (scaledWidth >= maxScaleWidth ||
                    (mime.toLowerCase().contains("gif") && scaledWidth >= maxScaleWidth / 2)) {
                displayWidth = maxViewWidth;
                displayHeight = Math.round(maxViewWidth * 1.0f * height / width);
            } else {
                displayWidth = scaledWidth;
                displayHeight = scaledHeight;
            }

            ImageReadyInfo imageReadyInfo = new ImageReadyInfo(cacheFile.getPath(), displayWidth, displayHeight, mime);
            ImageContainer.markImageReady(mUrl, imageReadyInfo);

            return imageReadyInfo;
        } catch (Exception e) {
            Logger.e(e);
        }
        return null;
    }

    private int dpToPx(int dp) {
        float density = mCtx.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    //Math! http://www.mathsisfun.com/data/function-grapher.php
    private double getScaleRate(int x) {
        return Math.pow(x, 1.2) / x;
    }

}
