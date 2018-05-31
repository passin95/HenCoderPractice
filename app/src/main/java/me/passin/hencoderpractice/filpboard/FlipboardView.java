package me.passin.hencoderpractice.filpboard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import me.passin.hencoderpractice.R;

/**
 * <pre>
 * @author : passin
 * Contact me : https://github.com/passin95
 * Date: 2018/5/28 14:04
 * </pre>
 */
public class FlipboardView extends View {

    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Camera camera = new Camera();
    Bitmap bitmap;
    int rightTurnoverDegree;
    int rotateTurnoverDegree;
    int topTurnoverDegree;

    public FlipboardView(Context context) {
        super(context);
    }

    public FlipboardView(Context context,
            @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FlipboardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_filpboard);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float newZ = -displayMetrics.density * 6;
        camera.setLocation(0, 0, newZ);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int x = centerX - bitmapWidth / 2;
        int y = centerY - bitmapHeight / 2;

        canvas.save();
        canvas.rotate(-rotateTurnoverDegree, centerX, centerY);
        canvas.clipRect(0, 0, centerX, getHeight());
        canvas.rotate(rotateTurnoverDegree, centerX, centerY);
//
        camera.save();
        camera.rotateX(-topTurnoverDegree);
        canvas.translate(centerX, centerY);
        camera.applyToCanvas(canvas);
        canvas.translate(-centerX, -centerY);
        camera.restore();;

//
        canvas.drawBitmap(bitmap, x, y, paint);
        canvas.restore();
//

        canvas.save();
        canvas.rotate(-rotateTurnoverDegree, centerX, centerY);
        canvas.clipRect(centerX, 0, getWidth(), getHeight());

        camera.save();
        camera.rotateY(-rightTurnoverDegree);
        canvas.translate(centerX, centerY);
        camera.applyToCanvas(canvas);
        canvas.translate(-centerX, -centerY);
        camera.restore();
        canvas.rotate(rotateTurnoverDegree, centerX, centerY);

        canvas.drawBitmap(bitmap, x, y, paint);
        canvas.restore();

    }

    public void setRightTurnoverDegree(int rightTurnoverDegree) {
        this.rightTurnoverDegree = rightTurnoverDegree;
        invalidate();
    }

    public void setRotateTurnoverDegree(int rotateTurnoverDegree) {
        this.rotateTurnoverDegree = rotateTurnoverDegree;
        invalidate();
    }

    public void setTopTurnoverDegree(int topTurnoverDegree) {
        this.topTurnoverDegree = topTurnoverDegree;
        invalidate();
    }

    public void clearCanvas() {
        rightTurnoverDegree = 0;
        rotateTurnoverDegree = 0;
        topTurnoverDegree = 0;
        invalidate();
    }
}
