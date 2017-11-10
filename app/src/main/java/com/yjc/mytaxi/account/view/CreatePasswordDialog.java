package com.yjc.mytaxi.account.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.yjc.mytaxi.MyTaxiApplication;
import com.yjc.mytaxi.R;
import com.yjc.mytaxi.account.model.AccountManagerImpl;
import com.yjc.mytaxi.account.model.IAccountManager;
import com.yjc.mytaxi.account.presenter.CreatePasswordDialogPresenterImpl;
import com.yjc.mytaxi.account.presenter.ICreatePasswordDialogPresenter;
import com.yjc.mytaxi.common.dataBus.RxBus;
import com.yjc.mytaxi.common.http.IHttpClient;
import com.yjc.mytaxi.common.http.impl.OkHttpClientImpl;
import com.yjc.mytaxi.common.storage.SharedPreferenceDao;
import com.yjc.mytaxi.common.util.ToastUtil;

/**
 * Created by Administrator on 2017/11/6/006.
 * 密码创建/修改
 */

public class CreatePasswordDialog extends Dialog implements ICreatePasswordDialogView{

    private static final String TAG="CreatePasswordDialog";
    private TextView mTitle;
    private TextView mPhone;
    private EditText mPw;
    private EditText mRePw;
    private Button mBtnConfirm;
    private View mLoading;
    private TextView mTips;
    private String mPhoneStr;
    private ICreatePasswordDialogPresenter mPresenter;

    public CreatePasswordDialog(@NonNull Context context,String phone) {
        this(context, R.style.Dialog);
        //从上一个页面传来的手机号
        mPhoneStr=phone;
        IHttpClient httpClient=new OkHttpClientImpl();
        SharedPreferenceDao dao=new SharedPreferenceDao(MyTaxiApplication.getInstance(),
                SharedPreferenceDao.FILE_ACCOUNT);
        IAccountManager manager=new AccountManagerImpl(httpClient,dao);
        mPresenter=new CreatePasswordDialogPresenterImpl(this, manager);
    }

    public CreatePasswordDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
    }

    protected CreatePasswordDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }



    @Override
    public void showLoading() {
        mLoading.setVisibility(View.VISIBLE);
    }

    @Override
    public void showError(int code, String msg) {
        mTips.setTextColor(getContext().getResources()
                .getColor(R.color.error_red));
        mTips.setText(getContext().getString(R.string.error_server));
    }

    @Override
    public void showLoginSuc() {
        dismiss();
        ToastUtil.show(getContext(),getContext().getString(R.string.login_suc));
    }


    /**
     * 显示注册成功
     */
    @Override
    public void showRegisterSuc() {
        mLoading.setVisibility(View.VISIBLE);
        mBtnConfirm.setVisibility(View.GONE);
        mTips.setVisibility(View.VISIBLE);
        mTips.setTextColor(getContext().getResources()
                .getColor(R.color.color_text_normal));
        mTips.setText(getContext()
                .getString(R.string.register_suc_and_login));
        // 请求网络，完成自动登录
        mPresenter.requestLogin(mPhoneStr, mPw.getText().toString());
        dismiss();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater= (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View root=inflater.inflate(R.layout.dialog_create_pw,null);
        setContentView(root);
        initViews();

        //注册 Presenter
        RxBus.getInstance().register(mPresenter);
    }

    private void initViews() {
        mPhone=findViewById(R.id.phone);
        mPw=findViewById(R.id.pw);
        mRePw=findViewById(R.id.pwl);
        mBtnConfirm=findViewById(R.id.btn_confirm);
        mLoading=findViewById(R.id.loading);
        mTips=findViewById(R.id.tips);
        mTitle=findViewById(R.id.dialog_title);
        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mBtnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
        mPhone.setText(mPhoneStr);
    }

    /**
     * 提交注册
     */
    private void submit() {
        if(checkPassword()){
            final String password=mPw.getText().toString();
            final String phone=mPhoneStr;
            mPresenter.requestRegister(phone,password);
        }
    }

    /**
     * 检查密码输入,不能为空，两次输入要一致
     * @return
     */
    private boolean checkPassword() {
        String password=mPw.getText().toString();
        if(TextUtils.isEmpty(password)){
            mTips.setVisibility(View.VISIBLE);
            mTips.setText(getContext().getString(R.string.password_is_null));
            mTips.setTextColor(getContext()
                    .getResources().getColor(R.color.error_red));
            return false;
        }
        if(!password.equals(mRePw.getText().toString())){
            mTips.setVisibility(View.VISIBLE);
            mTips.setText(getContext().getString(R.string.password_is_not_equal));
            mTips.setTextColor(getContext()
                    .getResources().getColor(R.color.error_red));
            return false;
        }
        return true;
    }

    @Override
    public void dismiss() {
        super.dismiss();

        //注销Presenter
        RxBus.getInstance().unRegister(mPresenter);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

}
