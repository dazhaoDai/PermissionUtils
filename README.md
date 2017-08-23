# PermissionUtils
封装了Android6.0运行时权限申请，简化了申请流程，提高开发效率


```
/**
 * @Author: ddz
 * Creation time: 17.8.11 17:11
 * describe:{Android权限申请的封装，并在内部实现申请结果的处理}
 */

public class PermissionUtils {

    private static PermissionUtils permission;
    private String[] permissions;
    private Activity mActivity;
    private RequestPermissionListener PermissionListener;


    public static int requestCode = 100;  //requestCode传值为100


    public static PermissionUtils getInstance() {
        if (null == permission) {
            synchronized (PermissionUtils.class) {
                if (null == permission) {
                    permission = new PermissionUtils();
                }
            }
        }
        return permission;
    }

    /**
     * 权限检查
     *
     * @param permission
     * @return
     */
    public boolean checkPermission(@NonNull String permission) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        } else {
            return ContextCompat.checkSelfPermission(LifeStyle.getContext(), permission) == PackageManager.PERMISSION_GRANTED;
        }
    }

    /**
     * Activity 页面申请权限
     *
     * @param activity
     * @param permissions
     * @param requestCode
     * @param requestPermissionListener
     */
    public void requestPermissiion(final @NonNull Activity activity,
                                   final @NonNull String[] permissions, final @IntRange(from = 0) int requestCode, @NonNull RequestPermissionListener requestPermissionListener) {
        this.mActivity = activity;
        PermissionListener = requestPermissionListener;
        this.permissions = permissions;
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }


    /**
     * Fragment页面申请权限
     *
     * @param fragment
     * @param permissions
     * @param requestCode
     * @param requestPermissionListener
     */
    public void requestFragmentPermission(final @NonNull Fragment fragment,
                                          final @NonNull String[] permissions, final @IntRange(from = 0) int requestCode, @NonNull RequestPermissionListener requestPermissionListener) {
        PermissionListener = requestPermissionListener;
        this.permissions = permissions;
        fragment.requestPermissions(permissions, requestCode);
    }


    /**
     * 权限申请结果的回调
     *
     * @param activity
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public void onRequestPermissionResult(final @NonNull Activity activity, int requestCode, @NonNull String[] permissions,
                                          @NonNull int[] grantResults) {
        this.mActivity = activity;
        if (null != PermissionListener) {
            if (requestCode == PermissionUtils.requestCode && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                PermissionListener.requestConfirm();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[0])) {
                    PermissionListener.requestCancel();
                } else {
                    PermissionListener.requestCancelAgain();
                }
            }
        }
    }


    /**
     * 用户点击拒绝，弹出申请权限的说明弹窗，也可以自定义实现
     *
     * @param context Context
     * @param title   弹窗标题
     * @param message 申请权限解释说明
     * @param confirm 确认按钮的文字，默认OK
     * @param cancel  取消按钮呢的文字，默认不显示取消按钮
     */

    public void requestDialog(Context context, @NonNull String title, @NonNull String message, String confirm, String cancel) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context).setTitle(title).setMessage(message);
            builder.setPositiveButton(confirm == null ? "OK" : confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    requestPermissiion(mActivity, permissions, requestCode, PermissionListener);
                    dialog.dismiss();
                }
            });
            if (null != cancel) {
                builder.setNegativeButton(cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
            }
            builder.setCancelable(false);
            builder.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 用户勾选不再显示并点击拒绝，弹出打开设置页面申请权限，也可以自定义实现
     *
     * @param context Context
     * @param title   弹窗标题
     * @param message 申请权限解释说明
     * @param confirm 确认按钮的文字，默认OK
     * @param cancel  取消按钮呢的文字，默认不显示取消按钮
     */

    public void requestDialogAgain(Context context, @NonNull String title, @NonNull String message, String confirm, String cancel) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context).setTitle(title).setMessage(message);
            builder.setPositiveButton(confirm == null ? "OK" : confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startSettingActivity(mActivity);
                    dialog.dismiss();
                }
            });
            if (null != cancel) {
                builder.setNegativeButton(cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
            }
            builder.setCancelable(false);
            builder.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 打开设置页面打开权限
     *
     * @param context
     */
    public void startSettingActivity(@NonNull Activity context) {

        try {
            Intent intent =
                    new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" +
                            context.getPackageName()));
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            context.startActivityForResult(intent, 10); //这里的requestCode和onActivityResult中requestCode要一致
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 打开设置页面的回调
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 10:  //这里值是打开设置页面申请权限的RequestCode，默认为10
                try {
                    if (null != PermissionListener) {
                        if (null != permissions && permissions.length > 0) {
                            for (String permission : permissions) {
                                if (checkPermission(permission)) {
                                    PermissionListener.requestConfirm();
                                } else {
                                    PermissionListener.requestFailed();
                                }
                            }
                        } else {
                            PermissionListener.requestFailed();
                        }
                    } else {
                        PermissionListener.requestFailed();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    /**
     * 权限申请回调
     */
    public interface RequestPermissionListener {
        void requestConfirm();  //申请成功

        void requestCancel();  //拒绝

        void requestCancelAgain();  //勾选不再提示并拒绝

        void requestFailed();  //在设置页面申请权限失败
    }
}
```
