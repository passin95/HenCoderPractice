package me.passin.hencoderpractice.thumbup;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;
import me.passin.hencoderpractice.R;
import me.passin.hencoderpractice.thumbup.ThumbUpView.ThumbUpClickListener;

/**
 * <pre>
 * @author : passin
 * Contact me : https://github.com/passin95
 * Date: 2018/5/29 10:03
 * </pre>
 */
public class ThumbUpLayout extends RelativeLayout {

    ThumbUpView mThumbUpView;
    Button btnAnimation;
    private EditText etNumber;
    private static Toast mToast;

    public ThumbUpLayout(Context context) {
        super(context);
    }

    public ThumbUpLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ThumbUpLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        mThumbUpView = findViewById(R.id.objectAnimatorView);
        etNumber = findViewById(R.id.et_number);
        btnAnimation = findViewById(R.id.btn_animate);

        mThumbUpView.setThumbUpClickListener(new ThumbUpClickListener() {
            @Override
            public void thumbUpFinish() {
                showToast("点赞");
            }

            @Override
            public void thumbDownFinish() {
                showToast("取消点赞");
            }
        });

        btnAnimation.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mThumbUpView.setCount(Integer.parseInt(etNumber.getText().toString().trim()));
            }
        });
    }

    public void showToast(String content) {
        if (mToast != null) {
            mToast.cancel();
            mToast = null;
        }
        mToast = Toast
                .makeText(getContext().getApplicationContext(), content, Toast.LENGTH_SHORT);
        mToast.show();
    }
}
