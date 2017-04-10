package com.xprinter.posconsend.activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.xprinter.posconsend.R;
import com.xprinter.posconsend.utils.StringUtils;

import net.posprinter.posprinterface.ProcessData;
import net.posprinter.posprinterface.UiExecute;
import net.posprinter.utils.BitmapToByteData;
import net.posprinter.utils.DataForSendToPrinterPos76;

import java.util.ArrayList;
import java.util.List;

public class Z76Activity extends AppCompatActivity {

    Button bttext,btimage;
    CoordinatorLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_z76);
        //初始化
        Log.e("bttext","1");
        initView();

    }
    //初始化控件
    private void initView(){
        Log.e("bttext","2");
        bttext= (Button) findViewById(R.id.bttext);
        btimage= (Button) findViewById(R.id.btpic);
        container= (CoordinatorLayout) findViewById(R.id.activity_z76);

        bttext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               showSnackbar("test11");
                printText();
            }
        });

        btimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent,0);
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("test",requestCode+"  "+resultCode);
        if (requestCode==0&&resultCode==RESULT_OK){
            Log.e("test","test2");
            //通过去图库选择图片，然后得到返回的bitmap对象
            try{
                Uri imagepath=data.getData();
                ContentResolver resolver = getContentResolver();
                Bitmap b= MediaStore.Images.Media.getBitmap(resolver,imagepath);
                prinPic(b);
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }


    //打印文本
    private void printText() {

        MainActivity.binder.writeDataByYouself(new UiExecute() {
            @Override
            public void onsucess() {
                showSnackbar("ok");
            }

            @Override
            public void onfailed() {

            }
        }, new ProcessData() {
            @Override
            public List<byte[]> processDataBeforeSend() {

                Log.e("bttext","bttext1");
                List<byte[]> list=new ArrayList<byte[]>();
                //创建一段我们想打印的文本,转换为byte[]类型，并添加到要发送的数据的集合list中
                String str = "Welcome to use the impact and thermal printer manufactured by professional POS receipt printer company!";
                byte[] data= StringUtils.strTobytes(str);
                list.add(DataForSendToPrinterPos76.initializePrinter());
                list.add(data);
                //追加一个打印换行指令，因为，pos打印机满一行才打印，不足一行，不打印
                list.add(DataForSendToPrinterPos76.printAndFeedLine());

                return list;
            }
        });


    }


    /*
    打印图片
     */
    private void prinPic(final Bitmap biimap) {
        MainActivity.binder.writeDataByYouself(new UiExecute() {
            @Override
            public void onsucess() {
                showSnackbar("ok");

            }

            @Override
            public void onfailed() {

            }
        }, new ProcessData() {
            @Override
            public List<byte[]> processDataBeforeSend() {
                List<byte[]>list=new ArrayList<byte[]>();
                //初始化
                list.add(DataForSendToPrinterPos76.initializePrinter());
                list.add(DataForSendToPrinterPos76.selectBmpModel(0,biimap, BitmapToByteData.BmpType.Dithering));
                return list;
            }
        });

    }

    /**
     * 显示提示信息
     * @param showstring 显示的内容字符串
     */
    private void showSnackbar(String showstring){
        Snackbar.make(container, showstring,Snackbar.LENGTH_LONG)
                .setActionTextColor(getResources().getColor(R.color.button_unable)).show();
    }

}
