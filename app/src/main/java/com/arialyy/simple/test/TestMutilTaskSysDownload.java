package com.arialyy.simple.test;

import android.os.Environment;
import android.view.View;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.common.QueueMod;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseActivity;
import com.arialyy.simple.databinding.TestActivityMultiBinding;

/**
 * Created by AriaL on 2017/6/15.
 */

public class TestMutilTaskSysDownload extends BaseActivity<TestActivityMultiBinding> {

  @Override protected int setLayoutId() {
    return R.layout.test_activity_multi;
  }

  public void onClick(View view) {
    String baseUrl = "http://file.bmob.cn/";
    String[] urlArray = {
        "M02/3B/A4/oYYBAFaOeUSAc1QiAAFTbmA7AHs052.jpg",
        "M02/3B/A4/oYYBAFaOeUaAfYC-AAFD8zf9NXc879.jpg",
        "M02/3B/A4/oYYBAFaOeUuAOxhnAACSdmbqSac702.jpg",
        "M02/3B/A4/oYYBAFaOeU2AFAIGAAFICximvXc924.jpg",
        "M02/3B/A4/oYYBAFaOeVCAPWMQAAFm2KWCq_E721.jpg",
        "M02/3B/A4/oYYBAFaOeVOAbiv9AAFfCTTgr94948.jpg",
        "M02/3B/A4/oYYBAFaOeVaAMR3tAAFf3yTuuCM577.jpg",
        "M02/3B/A4/oYYBAFaOeVmACEWhAAEt72ecbpg468.jpg",
        "M02/3B/A4/oYYBAFaOeVyAHHt4AAFg9e9bRio507.jpg",
        "M02/3B/A4/oYYBAFaOeV-AClYXAAESLGY0gag424.jpg",
        "M02/3B/A4/oYYBAFaOeWKAA7N0AAF3omYOJUI703.jpg",
        "M02/3B/A4/oYYBAFaOeWWAD2lrAAFN7eRFxBs575.jpg",
        "M02/3B/A4/oYYBAFaOeWiAdCVEAAFg4273Dus313.jpg",
        "M02/3B/A4/oYYBAFaOeWyAJDm5AAF8JVoGVb0705.jpg",
        "M02/3B/A4/oYYBAFaOeW-AUoA8AAGjKiHkXUo181.jpg",
        "M02/3B/A4/oYYBAFaOeXKABIamAAFU7J7vraE265.jpg",
        "M02/3B/A5/oYYBAFaOeXaAW09jAAFf37qdwDA457.jpg",
        "M02/3B/A5/oYYBAFaOeXmAWmS7AAFtLNpWjgo967.jpg",
        "M02/3B/A5/oYYBAFaOeX2AQf9cAAF2fhwS2UE145.jpg",
        "M02/3B/A5/oYYBAFaOeYCAKGnLAAFVAzks-qU937.jpg",
        "M02/3B/A5/oYYBAFaOeYOAMODNAAF6HjTTMq4819.jpg",
        "M02/3B/A5/oYYBAFaOeYeAbn8uAAFLSQLw48Q042.jpg",
        "M02/3B/A5/oYYBAFaOeYqAMJThAAFtrNe4UNM047.jpg",
        "M02/3B/A5/oYYBAFaOeY2AbnQvAAFNSXWn0Dc026.jpg",
        "M02/3B/A5/oYYBAFaOeZCAIsr0AAFHZFEVhPc682.jpg",
        "M02/3B/A5/oYYBAFaOeZOAGvITAAFqPmfcc9c471.jpg",
        "M02/3B/A5/oYYBAFaOeZaATvjbAAFHDmALnhE003.jpg",
        "M02/3B/A5/oYYBAFaOeZmAJPuVAAFfPJC2wsE319.jpg",
        "M02/3B/A5/oYYBAFaOeZyAXtAmAAFfArJNwtM371.jpg",
        "M02/3B/A5/oYYBAFaOeZ-AGZN0AAFgqwYYCS8004.jpg",
        "M02/3B/A5/oYYBAFaOeaOAbbrGAAFcq59JjUo205.jpg",
        "M02/3B/A5/oYYBAFaOeaSAdFyoAACaxVxgUJA092.jpg"
    };
    int maxNum = Aria.get(this).getDownloadConfig().getMaxTaskNum();
    Aria.get(this).setDownloadQueueMod(QueueMod.NOW);
    for (int i = 0; i < urlArray.length; i++) {
      Aria.download(this)
          .load(baseUrl + urlArray[i])
          .setDownloadPath(Environment.getExternalStorageDirectory() + "/test/" + i + ".jpg")
          //.addHeader("Accept-Encoding", "gzip,deflate,sdcn")
          .start();
      //if (i < maxNum) {
      //  Aria.download(this)
      //      .load(baseUrl + urlArray[i])
      //      .setDownloadPath(Environment.getExternalStorageDirectory() + "/test/" + i + ".jpg")
      //      //.addHeader("Accept-Encoding", "gzip,deflate,sdcn")
      //      .start();
      //} else {
      //  Aria.download(this)
      //      .load(baseUrl + urlArray[i])
      //      .setDownloadPath(Environment.getExternalStorageDirectory() + "/test/" + i + ".jpg")
      //      //.addHeader("Accept-Encoding", "gzip,deflate,sdcn")
      //      .add();
      //}
    }
  }
}
