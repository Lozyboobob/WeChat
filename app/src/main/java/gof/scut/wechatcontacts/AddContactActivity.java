package gof.scut.wechatcontacts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.gson.Gson;

import java.util.Hashtable;

import gof.scut.common.MyApplication;
import gof.scut.common.utils.Log;
import gof.scut.common.utils.database.MainTableUtils;
import gof.scut.cwh.models.object.IdObj;

public class AddContactActivity extends Activity {

	private static final String TAG = AddContactActivity.class.getSimpleName();
	private final Context mContext = AddContactActivity.this;

	private final static int SCANNIN_GREQUEST_CODE = 1;
	private MainTableUtils mainTableUtils;
	private Gson gson;

	//view
	private ImageView imageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_contact);

		initView();
	}

	private void initView() {
		//点击按钮跳转到二维码扫描界面，这里用的是startActivityForResult跳转
		//扫描完了之后调到该界面
		findViewById(R.id.scan).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(AddContactActivity.this, gof.scut.common.zixng.codescan.MipcaActivityCapture.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
			}
		});

		mainTableUtils = new MainTableUtils(mContext);
		gson = MyApplication.getGson();


		imageView = (ImageView) findViewById(R.id.imageView);

		IdObj obj = new IdObj(101, "周萌", "zhoumeng", "zm", "13660567470", "华工", "no");
		createQRImage(gson.toJson(obj, IdObj.class));

	}

	public void createQRImage(String url) {
		int QR_WIDTH = 1000;
		int QR_HEIGHT = 1000;
		try {
			//判断URL合法性
			if (url == null || "".equals(url) || url.length() < 1) {
				return;
			}
			Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
			hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
			//图像数据转换，使用了矩阵转换
			BitMatrix bitMatrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
			int[] pixels = new int[QR_WIDTH * QR_HEIGHT];
			//下面这里按照二维码的算法，逐个生成二维码的图片，
			//两个for循环是图片横列扫描的结果
			for (int y = 0; y < QR_HEIGHT; y++) {
				for (int x = 0; x < QR_WIDTH; x++) {
					if (bitMatrix.get(x, y)) {
						pixels[y * QR_WIDTH + x] = 0xff000000;
					} else {
						pixels[y * QR_WIDTH + x] = 0xffffffff;
					}
				}
			}
			//生成二维码图片的格式，使用ARGB_8888
			Bitmap bitmap = Bitmap.createBitmap(QR_WIDTH, QR_HEIGHT, Bitmap.Config.ARGB_8888);
			bitmap.setPixels(pixels, 0, QR_WIDTH, 0, 0, QR_WIDTH, QR_HEIGHT);
			//显示到一个ImageView上面
			imageView.setImageBitmap(bitmap);
		} catch (WriterException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case SCANNIN_GREQUEST_CODE:
				if (resultCode == RESULT_OK) {
					Bundle bundle = data.getExtras();
					//显示
//                    mImageView.setImageBitmap((Bitmap) data.getParcelableExtra("bitmap"));

					String resultString = bundle.getString("result");

					IdObj object = gson.fromJson(resultString, IdObj.class);
					mainTableUtils.insertAll(object.getName(),
							object.getlPinYin(), object.getsPinYin(), object.getAddress()
							, object.getNotes());

					Log.d(null, object.toString());
				}
				break;
		}
	}
}
