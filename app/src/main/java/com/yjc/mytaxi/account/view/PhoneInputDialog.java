package com.yjc.mytaxi.account.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.yjc.mytaxi.R;
import com.yjc.mytaxi.common.util.FormaUtil;

/**
 * Created by Administrator on 2017/11/1/001.
 */

public class PhoneInputDialog extends Dialog {

    private View mRoot;
    private EditText mPhone;
    private Button mButton;

    public PhoneInputDialog(@NonNull Context context) {
        this(context, R.style.Dialog);
    }

    public PhoneInputDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
    }

    protected PhoneInputDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater= (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRoot=inflater.inflate(R.layout.dialog_phone_input,null);
        setContentView(mRoot);
        initListener();
    }

    private void initListener() {
        mButton= (Button) findViewById(R.id.btn_next);
        mButton.setEnabled(false);
        mPhone= (EditText) findViewById(R.id.phone);
        //手机号输入框注册监听检查手机号输入是否合法
        mPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                check();
            }
        });

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                String phone=mPhone.getText().toString();
                SmsCodeDialog dialog=new SmsCodeDialog(getContext(),phone);
                dialog.show();
            }
        });

        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }


    /**
     * 检查手机号码是否合法
     */
    private void check() {
        String phone=mPhone.getText().toString();
        boolean legal= FormaUtil.checkMobile(phone);
        mButton.setEnabled(legal);
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }
}
