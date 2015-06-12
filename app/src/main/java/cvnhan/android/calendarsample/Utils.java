package cvnhan.android.calendarsample;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;

/**
 * Created by cvnhan on 08-Jun-15.
 */
public class Utils {


    public static Bitmap circleBitmap(Bitmap bitmap) {
        Paint paint = new Paint();
        Bitmap mutableBitmap = Bitmap.createBitmap(bitmap);
        float w, h;
        w = mutableBitmap.getWidth();
        h = mutableBitmap.getHeight();
        Bitmap bitmapView = Bitmap.createBitmap((int) w, (int) h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapView);

        BitmapShader shader;
        shader = new BitmapShader(mutableBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        // init paint
        paint.setAntiAlias(true);
        paint.setShader(shader);
        canvas.drawCircle(w / 2, h / 2, w / 2 - 1, paint);
        Paint circleRound= new Paint();
        circleRound.setAntiAlias(true);
        circleRound.setStrokeWidth(1.5f);
        circleRound.setStyle(Paint.Style.STROKE);
        circleRound.setColor(Color.parseColor("#cccccc"));
        canvas.drawCircle(w/2, h/2, w/2-0.5f, circleRound);
        return bitmapView;
    }

    public static Bitmap circleBitmapSelected(Bitmap bitmap) {
        Paint paint = new Paint();
        Bitmap mutableBitmap = Bitmap.createBitmap(bitmap);
        float w, h;
        w = mutableBitmap.getWidth();
        h = mutableBitmap.getHeight();
        Bitmap bitmapView = Bitmap.createBitmap((int) w, (int) h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapView);

        BitmapShader shader;
        shader = new BitmapShader(mutableBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        // init paint
        paint.setAntiAlias(true);
        paint.setShader(shader);
        canvas.drawCircle(w / 2, h / 2, w / 2 - 1, paint);
        Paint circleRound= new Paint();
        circleRound.setAntiAlias(true);
        circleRound.setStrokeWidth(1.5f);
        circleRound.setStyle(Paint.Style.STROKE);
        circleRound.setColor(Color.parseColor("#f6921e"));
        canvas.drawCircle(w/2, h/2, w/2-0.5f, circleRound);
        return bitmapView;
    }


}
