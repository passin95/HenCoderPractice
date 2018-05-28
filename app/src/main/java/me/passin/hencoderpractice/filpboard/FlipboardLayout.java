package me.passin.hencoderpractice.filpboard;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.RelativeLayout;
import me.passin.hencoderpractice.R;

/**
 * <pre>
 * @author : passin
 * Contact me : https://github.com/passin95
 * Date: 2018/5/28 14:03
 * </pre>
 */
public class FlipboardLayout extends RelativeLayout{

    FlipboardView view;
    Button animateBt;
    private AnimatorSet animatorSet;

    public FlipboardLayout(Context context) {
        super(context);
    }

    public FlipboardLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FlipboardLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        view = findViewById(R.id.objectAnimatorView);
        animateBt = findViewById(R.id.animateBt);

        animateBt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                view.clearCanvas();

                if (animatorSet != null && animatorSet.isRunning()) {
                    animatorSet.cancel();
                }

                ObjectAnimator animator1 = ObjectAnimator.ofInt(view, "rightTurnoverDegree", 0, 30);
                animator1.setDuration(1000);
                animator1.setInterpolator(new LinearInterpolator());

                ObjectAnimator animator2 = ObjectAnimator.ofInt(view, "rotateTurnoverDegree", 0, 270);
                animator2.setDuration(2000);
                animator2.setInterpolator(new AccelerateDecelerateInterpolator());

                ObjectAnimator animator3 = ObjectAnimator.ofInt(view, "topTurnoverDegree", 0, 30);
                animator3.setDuration(1000);
                animator3.setInterpolator(new LinearInterpolator());

                animatorSet = new AnimatorSet();
                // 两个动画依次执行
                animatorSet.playSequentially(animator1, animator2, animator3);
                animatorSet.start();
            }
        });
    }


}
