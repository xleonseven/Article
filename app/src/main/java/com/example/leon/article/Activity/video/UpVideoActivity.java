package com.example.leon.article.Activity.video;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;

import com.example.leon.article.Activity.MainActivity;
import com.example.leon.article.Activity.art.ArtConstant;
import com.example.leon.article.Activity.record.camer.MediaRecorderActivity;
import com.example.leon.article.Activity.record.camer.common.CommonIntentExtra;
import com.example.leon.article.Activity.record.camer.commonutil.utils.ToastUitl;
import com.example.leon.article.Activity.record.camer.utils.ToastUtils;
import com.example.leon.article.Activity.video.permission.PermissionsActivity;
import com.example.leon.article.Activity.video.permission.PermissionsChecker;
import com.example.leon.article.Activity.video.videocompress.CompressListener;
import com.example.leon.article.Activity.video.videocompress.Compressor;
import com.example.leon.article.Activity.video.videocompress.InitListener;
import com.example.leon.article.R;
import com.example.leon.article.api.ApiManager;
import com.example.leon.article.api.bean.UpLoadArtBean;
import com.example.leon.article.api.bean.UploadClassifyBean;
import com.example.leon.article.presenter.videopresenter.videopresenterImp.VideoPresenterImp;
import com.example.leon.article.utils.Constant;
import com.example.leon.article.utils.CreateBitmap;
import com.example.leon.article.utils.SPUtil;
import com.example.leon.article.utils.UriAllUriUtils;
import com.example.leon.article.view.IUpVideoActivity;
import com.example.leon.article.widget.FllScreenVideoView;
import com.example.leon.article.widget.SelectVideoPopupWindow;
import com.example.leon.article.widget.SpinnerDialog;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;
import kr.co.namee.permissiongen.PermissionSuccess;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class UpVideoActivity extends AppCompatActivity implements View.OnClickListener, SelectVideoPopupWindow.OnSelectedListener, IUpVideoActivity {

    private static final int VIDEO_KU = 12345;
    private EditText et_title;
    private MaterialSpinner mSpinner;
    private Button bt_addvideo, bt_videolist, bt_upload;
    private SelectVideoPopupWindow selectVideoPopupWindow;
    private FllScreenVideoView mVideoView;
    private ImageView iv_play;
    private FrameLayout fl_myVideo;
    // 获取分类信息集合
    private ArrayList<UploadClassifyBean.DataBean> classifys = new ArrayList<>();
    private List<String> items = new ArrayList<>();
    private int selectPosition = 1;
    private VideoPresenterImp videoPresenterImp;
    private ImageView iv_cover, iv_back;
    private String bytesFromBitmap;
    private EditText et_videoContent;
    private Dialog spinnerDialog;
    /*录制视频参数*/
    private String currentInputVideoPath = "";
    private String currentOutputVideoPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath()+"/WCXW/out.mp4";

    private Double videoLength = 0.00;//视频时长 s
    private Compressor mCompressor;
    private static final int REQUEST_CODE_FOR_PERMISSIONS = 0;//
    private static final int REQUEST_CODE_FOR_RECORD_VIDEO = 1;//录制视频请求码
    public static final int RESULT_CODE_FOR_RECORD_VIDEO_SUCCEED = 2;//视频录制成功
    public static final int RESULT_CODE_FOR_RECORD_VIDEO_FAILED = 3;//视频录制出错
    public static final int RESULT_CODE_FOR_RECORD_VIDEO_CANCEL = 4;//取消录制
    public static final String INTENT_EXTRA_VIDEO_PATH = "intent_extra_video_path";//录制的视频路径

    public static final String RECORD_VIDEO_PATH = "Record_VideoPath";

    //相机权限,录制音频权限,读写sd卡的权限,都为必须,缺一不可
    private static final String[] PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};

    private ProgressDialog mProgressDialog;
    private File mFile;
    private Bitmap firstBitmap;
    private String mRecordPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_up_video);
        initView();
        GetIntentData();
        GetData();
        initEvent();
    }

    private void GetIntentData() {
        Intent intent = getIntent();
        String videoTitle = intent.getStringExtra(CommonIntentExtra.VIDEO_TITLE);
        String videoContent = intent.getStringExtra(CommonIntentExtra.VIDEO_CONTENT);
        if (!TextUtils.isEmpty(videoContent) || !TextUtils.isEmpty(videoTitle)) {
            et_title.setText(videoTitle);
            et_videoContent.setText(videoContent);
        }
    }

    private void GetData() {
        //获取录制成功后的视频地址
        Intent intent = getIntent();
        if (intent != null) {
            mRecordPath = intent.getStringExtra(UpVideoActivity.RECORD_VIDEO_PATH);
            if (!TextUtils.isEmpty(mRecordPath)) {
//                Log.i("FiDo", "GetRecordPath: "+ mRecordPath);
                //显示图片和播放地址
                firstBitmap = CreateBitmap.getLocalVideoThumbnail(mRecordPath);
                iv_cover.setImageBitmap(firstBitmap);
                fl_myVideo.setVisibility(View.VISIBLE);
                mVideoView.setVideoPath(mRecordPath);
            }
        }
        //初始化presenter
        videoPresenterImp = new VideoPresenterImp(this);
        videoPresenterImp.getClassify(getCookie(), getSid());
    }

    private void initSpinner() {
        for (UploadClassifyBean.DataBean classify : classifys) {
            items.add(classify.getClass_name());
        }
        mSpinner.setItems(items);
        mSpinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                selectPosition = position + 1;
            }
        });
    }

    private void initView() {
        spinnerDialog = SpinnerDialog.createSpinnerDialog(this, "视频上传中...");
        mSpinner = (MaterialSpinner) findViewById(R.id.spinner_videoList);
        iv_cover = (ImageView) findViewById(R.id.iv_cover_upVideo);
        et_title = (EditText) findViewById(R.id.et_video);
        et_videoContent = (EditText) findViewById(R.id.et_videoContent);
        iv_back = (ImageView) findViewById(R.id.iv_upload_back);
        iv_play = (ImageView) findViewById(R.id.iv_start);
        fl_myVideo = (FrameLayout) findViewById(R.id.fl_myVideo);
        bt_addvideo = (Button) findViewById(R.id.bt_upvideo_addvideo);
        bt_videolist = (Button) findViewById(R.id.bt_upvideo_videolist);
        bt_upload = (Button) findViewById(R.id.bt_upvideo_upload);
        selectVideoPopupWindow = new SelectVideoPopupWindow(this);
        selectVideoPopupWindow.setOnSelectedListener(this);
        mVideoView = (FllScreenVideoView) findViewById(R.id.videoView);
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(mVideoView);
        mediaController.setVisibility(View.INVISIBLE);
        mVideoView.setMediaController(mediaController);
        mVideoView.requestFocus();
        //设置正文空格
        et_videoContent.setText("\t\t");
        et_videoContent.setSelection(et_videoContent.getText().length());

        //初始化权限
        PermissionsChecker mChecker = new PermissionsChecker(getApplicationContext());
        if (mChecker.lacksPermissions(PERMISSIONS)) {
            PermissionsActivity.startActivityForResult(this, REQUEST_CODE_FOR_PERMISSIONS, PERMISSIONS);
        }
        mCompressor = new Compressor(this);
        mCompressor.loadBinary(new InitListener() {
            @Override
            public void onLoadSuccess() {
            }

            @Override
            public void onLoadFail(String reason) {
            }
        });
    }

    private void initEvent() {
        bt_addvideo.setOnClickListener(this);
        bt_videolist.setOnClickListener(this);
        bt_upload.setOnClickListener(this);
        iv_play.setOnClickListener(this);//点击开始播放
        iv_back.setOnClickListener(this);
        //设置点击空白处消失popwindow
        selectVideoPopupWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    selectVideoPopupWindow.dismissPopupWindow();
                    return true;
                }
                return false;
            }
        });

        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // TODO Auto-generated method stub
                iv_play.setVisibility(View.VISIBLE);
//                iv_cover.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_upvideo_addvideo: //添加视频
                selectVideoPopupWindow.showPopupWindow(UpVideoActivity.this);
                break;
            case R.id.bt_upvideo_videolist://视频列表
                GoVideoListFragment();
                break;
            case R.id.bt_upvideo_upload://上传视频
                if (!TextUtils.isEmpty(mRecordPath)) {
                    uploaduserVideo(mRecordPath);
                }else{
                    ToastUitl.showLong("视频地址好像出错啦，请重试一下吧");
                }
                break;
            case R.id.iv_start:   //播放视频
                mVideoView.start();
                iv_cover.setVisibility(View.INVISIBLE);//隐藏第一帧图
                iv_play.setVisibility(View.GONE);
                break;
            case R.id.iv_upload_back:     //点击返回
                finish();
                break;
        }
    }

    private void execCommand(String cmd) {
        mFile = new File(currentOutputVideoPath);
        if (mFile.exists()) {
            mFile.delete();
        }
        mCompressor.execCommand(cmd, new CompressListener() {
            @Override
            public void onExecSuccess(String message) {
                hidenProgress();
                Toast.makeText(UpVideoActivity.this, "视频已准备完成", Toast.LENGTH_SHORT).show();
                //压缩成功显示图片和地址
                firstBitmap = CreateBitmap.getLocalVideoThumbnail(mRecordPath);
                iv_cover.setImageBitmap(firstBitmap);
                fl_myVideo.setVisibility(View.VISIBLE);
                mVideoView.setVideoPath(mRecordPath);
            }

            @Override
            public void onExecFail(String reason) {
                ToastUtils.showToast("视频地址错误，请重新选取");
                fl_myVideo.setVisibility(View.INVISIBLE);
                hidenProgress();
            }

            @Override
            public void onExecProgress(String message) {
                showProgress("", "视频准备中...", -1);
            }
        });
    }

    private void GoVideoListFragment() {
        int myVideoVisibility = fl_myVideo.getVisibility();
        if (!TextUtils.isEmpty(et_title.getText().toString().trim()) && myVideoVisibility == View.VISIBLE) {
            showifsaveDialog();
        } else {
            GoVideoFragment();
        }
    }

    private void GoVideoFragment() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(ArtConstant.SHOW_ARTICLEFRAGMENT, 1);
        intent.putExtra(ArtConstant.SHOW_ARTICLE_VIDEOFRAGMENT, 2017);
        startActivity(intent);
        finish();
    }

    private void showifsaveDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("您还没有上传哦，确定退出吗？")
                .setMessage("退出后数据将不会保存")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing or what you want
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .create()
                .show();
    }

    private void uploaduserVideo(String path) {
        mVideoView.pause();
        spinnerDialog.show();
        //获取视频第一帧图片
//        firstBitmap = CreateBitmap.getLocalVideoThumbnail(path);
        if (firstBitmap != null) {
            bytesFromBitmap = getBytesFromBitmap(firstBitmap);
        }
        //获取Title
        String title = et_title.getText().toString();
        String content = et_videoContent.getText().toString().trim();
        mFile = new File(path);
        if (!TextUtils.isEmpty(title) && firstBitmap != null && !TextUtils.isEmpty(content)) {
            RequestBody requestFile =
                    RequestBody.create(MediaType.parse("multipart/form-data"), mFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("video", mFile.getName(), requestFile);
            // 执行请求
            RequestBody body1 = RequestBody.create(MediaType.parse("multipart/form-data"), getSid());
            RequestBody body2 = RequestBody.create(MediaType.parse("multipart/form-data"), getCookie());
            RequestBody body3 = RequestBody.create(MediaType.parse("multipart/form-data"), title);
            RequestBody body4 = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(selectPosition));
            RequestBody body5 = RequestBody.create(MediaType.parse("multipart/form-data"), content);
            RequestBody body6 = RequestBody.create(MediaType.parse("multipart/form-data"), bytesFromBitmap);
            Map<String, RequestBody> map = new HashMap<>();
            map.put("sid", body1);
            map.put("cookie", body2);
            map.put("title", body3);
            map.put("class_id", body4);
            map.put("content", body5);
            map.put("img", body6);
            ApiManager.getInstance().getArtApiService()
                    .uploadVideo(map, body)
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<UpLoadArtBean>() {
                        @Override
                        public void onCompleted() {
                            spinnerDialog.dismiss();
                        }

                        @Override
                        public void onError(Throwable e) {
                            spinnerDialog.dismiss();
                            Toast.makeText(UpVideoActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onNext(final UpLoadArtBean upLoadArtBean) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    spinnerDialog.dismiss();
                                    if (upLoadArtBean.getCode().equals("1")) {
                                        Toast.makeText(UpVideoActivity.this, "上传成功", Toast.LENGTH_SHORT).show();
                                        GoVideoFragment();
                                    } else {
                                        Toast.makeText(UpVideoActivity.this, "上传失败", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }, 600);
                        }
                    });
        } else {
            if (path != null && fl_myVideo.getVisibility() == View.VISIBLE) {
                Toast.makeText(UpVideoActivity.this, "别忘了给视频加上标题与内容哦~", Toast.LENGTH_SHORT).show();
                spinnerDialog.dismiss();
            } else {
                Toast.makeText(UpVideoActivity.this, "别忘了添加您的视频哦~", Toast.LENGTH_SHORT).show();
                spinnerDialog.dismiss();
            }
        }
    }

    @Override
    public void OnSelected(View v, int position) {
        switch (position) {
            case 0:
                // "录制"按钮被点击了
                recording();
                selectVideoPopupWindow.dismissPopupWindow();
                break;
            case 1:
                // "从相册选择"按钮被点击了
                selectorFromGallery();
                selectVideoPopupWindow.dismissPopupWindow();
                break;
            case 2:
                // "取消"按钮被点击了
                selectVideoPopupWindow.dismissPopupWindow();
                break;
        }
    }

    private void selectorFromGallery() {
        PermissionGen.needPermission(this, VIDEO_KU, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE});
    }

    private void recording() {
        //录制
//        CameraActivity.startActivityForResult(this, REQUEST_CODE_FOR_RECORD_VIDEO);
        String t = et_title.getText().toString();
        String c = et_videoContent.getText().toString();
        startActivity(new Intent(this, MediaRecorderActivity.class).putExtra(CommonIntentExtra.VIDEO_TITLE,t)
                .putExtra(CommonIntentExtra.VIDEO_CONTENT,c));
        finish();
    }

    @PermissionSuccess(requestCode = VIDEO_KU)
    private void selectFromGallery() {
        // TODO 启动相册
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, VIDEO_KU);
    }


    @PermissionFail(requestCode = VIDEO_KU)
    private void showTip1() {
        showRequestDialog();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri = null;
        if (requestCode == REQUEST_CODE_FOR_PERMISSIONS) {
            //权限申请
            if (PermissionsActivity.PERMISSIONS_DENIED == resultCode) {
                //权限未被授予，退出应用
                finish();
            } else if (PermissionsActivity.PERMISSIONS_GRANTED == resultCode) {
                //权限被授予
                //do nothing
            }
        } else if (requestCode == REQUEST_CODE_FOR_RECORD_VIDEO) {
            //录制视频
            if (resultCode == RESULT_CODE_FOR_RECORD_VIDEO_SUCCEED) {
                //录制成功
                String videoPath = data.getStringExtra(INTENT_EXTRA_VIDEO_PATH);
                if (!TextUtils.isEmpty(videoPath)) {
                    currentInputVideoPath = videoPath;
                    MediaMetadataRetriever retr = new MediaMetadataRetriever();
                    retr.setDataSource(currentInputVideoPath);
                    String time = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);//获取视频时长
                    String mWidth = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);//宽
                    String mHeight = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);//高
                    double scale = Double.parseDouble(mWidth) / Double.parseDouble(mHeight);
                    execCommand("-y -i " + currentInputVideoPath + " -strict -2 -vcodec libx264 -preset ultrafast " +
                            "-crf 19 -acodec aac -ar 44100 -ac 2 -b:a 96k -s " + getUploadScale(Double.parseDouble(mWidth), Double.parseDouble(mHeight), scale) + " -aspect 16:9 " + currentOutputVideoPath);
                    try {
                        videoLength = Double.parseDouble(time) / 1000.00;
                    } catch (Exception e) {
                        e.printStackTrace();
                        videoLength = 0.00;
                    }
                }
            } else if (resultCode == RESULT_CODE_FOR_RECORD_VIDEO_FAILED) {
                //录制失败
                currentInputVideoPath = "";
                fl_myVideo.setVisibility(View.INVISIBLE);
            }
        } else if (requestCode == VIDEO_KU) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    uri = data.getData();
//                    currentInputVideoPath = UriAllUriUtils.getPath(this, uri);
                    mRecordPath = UriAllUriUtils.getPath(this, uri);
//                    Log.i("FiDo", "选取相册文件压缩路径为: "+mRecordPath);
                    MediaMetadataRetriever retr = new MediaMetadataRetriever();
                    retr.setDataSource(mRecordPath);
//                    String time = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);//获取视频时长
                    String mWidth = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);//宽
                    String mHeight = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);//高
                    double scale = Double.parseDouble(mWidth) / Double.parseDouble(mHeight);
                    if (uri != null) {
                        execCommand("-y -i " + mRecordPath + " -strict -2 -vcodec libx264 -preset ultrafast " +
                                "-crf 19 -acodec aac -ar 44100 -ac 2 -b:a 96k -s " + getUploadScale(Double.parseDouble(mWidth), Double.parseDouble(mHeight), scale) + " -aspect 16:9 " + currentOutputVideoPath);
                    }
                } catch (Exception e) {
                    String a = e + "";
                } catch (OutOfMemoryError e) {
                    String a = e + "";
                }
            }
        }
    }

    private String getUploadScale(double mWidth, double mHeight, double scale) {
        int width;
        int height;
        if (scale > 1) {//宽屏
            if (mWidth <= 480) {
                height = (int) mWidth;
                width = (int) mHeight;
                Log.i("FiDo", "宽频<= 480: " + width + "x" + height);
                return width + "x" + height;
            } else {
                height = (int) (mWidth / scale);
                Log.i("FiDo", "宽屏>480: " + 480 + "x" + height);
                return 480 + "x" + height;
            }
        } else {    //竖屏
            if (mHeight <= 640) {
                Log.i("FiDo", "竖屏<=640: " + mWidth + "x" + mHeight);
                return (int) mWidth + "x" + (int) mHeight;
            } else {
                mWidth = 640 * scale;
                Log.i("FiDo", "竖屏>640: " + mWidth + "x" + 640);
                return (int) mWidth + "x" + 640;
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (TextUtils.isEmpty(et_title.getText().toString().trim()) && TextUtils.isEmpty(et_videoContent.getText().toString())) {//如果输入的内容为空
            finish();
        } else {//用户输入的内容不为空
            showifsaveDialog();
        }
    }

    public String getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 50, baos);
        byte[] bytes = baos.toByteArray();
        String imgString = new String(Base64.encodeToString(bytes, Base64.DEFAULT));
        return imgString;
    }

    public static String getCookie() {
        return (String) SPUtil.get(Constant.Share_prf.COOKIE, "");
    }

    public static String getSid() {
        return (String) SPUtil.get(Constant.Share_prf.SID, "");
    }

    @Override
    public void showProgress() {
    }

    @Override
    public void hideProgress() {
    }

    @Override
    public void showError() {
    }

    @Override
    public void setClassfiy(UploadClassifyBean classifyBean) {
        if (classifyBean != null) {
            classifys.addAll(classifyBean.getData());
        }
        initSpinner();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoPresenterImp.unsubcrible();
        if (spinnerDialog != null) {
            spinnerDialog.cancel();
            spinnerDialog = null;
        }
        if (mVideoView != null) {
            mVideoView = null;
        }
    }

    private void showRequestDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //设置对话框显示小图标
        builder.setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("申请权限")
                .setMessage("在设置-应用-微创秀文-权限 中开启相机、存储权限，才能正常使用录像或相册选择功能")
                //添加确定按钮点击事件
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //这里用来跳到手机设置页，方便用户开启权限
                        Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                        intent.setData(Uri.parse("package:" + UpVideoActivity.this.getPackageName()));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                })
                //取消按钮点击事件
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing or what you want
                    }
                })
                .create()
                .show();
    }

    private void showProgress(String title, String message, int theme) {
        if (mProgressDialog == null) {
            if (theme > 0)
                mProgressDialog = new ProgressDialog(this, theme);
            else
                mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mProgressDialog.setCanceledOnTouchOutside(false);// 不能取消
            mProgressDialog.setCancelable(false);
            mProgressDialog.setIndeterminate(true);// 设置进度条是否不明确
        }

        if (!TextUtils.isEmpty(title))
            mProgressDialog.setTitle(title);
        mProgressDialog.setMessage(message);
        mProgressDialog.show();
    }

    private void hidenProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }


}
