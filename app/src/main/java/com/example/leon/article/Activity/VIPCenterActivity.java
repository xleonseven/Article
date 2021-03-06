package com.example.leon.article.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.widget.Toast;

import com.example.leon.article.Activity.bank.BankSettingActivity;
import com.example.leon.article.fragment.ArticleFragment;
import com.example.leon.article.R;
import com.example.leon.article.adapter.VIPCenterAdapter;
import com.example.leon.article.base.OnItemClickListener;
import com.example.leon.article.base.ToolBarBaseActivity;
import com.example.leon.article.bean.ItemBean;
import com.example.leon.article.databinding.ActivityVipCenterBinding;
import com.example.leon.article.utils.CommonUtils;

import java.util.ArrayList;

public class VIPCenterActivity extends ToolBarBaseActivity<ActivityVipCenterBinding> {


    private VIPCenterAdapter vipCenterAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vip_center);
        initView();
        initEvent();
    }

    private void initEvent() {
        ArrayList<ItemBean> data = new ArrayList<>();
        data.add(new ItemBean(R.drawable.basic_info, CommonUtils.getString(R.string.basic_info),
                BasicinformationActivity.class));
        data.add(new ItemBean(R.drawable.banks_setting, CommonUtils.getString(R.string.banks_setting),
                BankSettingActivity.class));
        data.add(new ItemBean(R.drawable.banks_setting, CommonUtils.getString(R.string.withdraw_deposit),
                WithdrawDepositActivity.class));
        data.add(new ItemBean(R.drawable.articles_list, CommonUtils.getString(R.string.articles_list),
                ArticleFragment.class));
        data.add(new ItemBean(R.drawable.password_modify, CommonUtils.getString(R.string.password_modify),
                ModifyPasswordActivity.class));
        binding.rvVipCenter.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        binding.rvVipCenter.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        vipCenterAdapter = new VIPCenterAdapter(data);
        binding.rvVipCenter.setAdapter(vipCenterAdapter);

        loadUserData();
    }

    public void initView() {
        setTitle(getString(R.string.vip_center));
    }


    private void onClick() {

        vipCenterAdapter.setOnItemClickListener(new OnItemClickListener<ItemBean>() {
            @Override
            public void onClick(ItemBean itemBean, int position) {
                Toast.makeText(VIPCenterActivity.this, "asd" + position, Toast.LENGTH_LONG).show();
                startActivity(new Intent(VIPCenterActivity.this, itemBean.getClazz()));
            }
        });
    }

}
