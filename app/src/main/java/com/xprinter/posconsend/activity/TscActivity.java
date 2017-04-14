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
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.xprinter.posconsend.R;
import com.xprinter.posconsend.utils.StringUtils;

import net.posprinter.posprinterface.ProcessData;
import net.posprinter.posprinterface.UiExecute;
import net.posprinter.utils.BitmapToByteData;
import net.posprinter.utils.DataForSendToPrinterTSC;

import java.util.ArrayList;
import java.util.List;

public class TscActivity extends AppCompatActivity implements View.OnClickListener{


    Button btcontent,
            bttsctext,
            bttscbarcode,
            bttscread,
            bttscpic;
    CoordinatorLayout container;

    RelativeLayout relativeLayout;
    ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tsc);
        //初始化控件
        initView();
        //设置监听
        setlisener();
    }

    private void setlisener() {
        btcontent.setOnClickListener(this);
        bttsctext.setOnClickListener(this);
        bttscbarcode.setOnClickListener(this);
        bttscread.setOnClickListener(this);
        bttscpic.setOnClickListener(this);

    }

    private void initView() {
        btcontent= (Button) findViewById(R.id.content);
        bttsctext= (Button) findViewById(R.id.tsctext);
        bttscbarcode= (Button) findViewById(R.id.tscbarcode);
        bttscread= (Button) findViewById(R.id.tscread);
        bttscpic= (Button) findViewById(R.id.tscpic);
        relativeLayout= (RelativeLayout) findViewById(R.id.rlimage);
        imageView= (ImageView) findViewById(R.id.image);
        container = (CoordinatorLayout) findViewById(R.id.activity_tsc);
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.content:
                printContent();
                break;
            case R.id.tsctext:
                printText();
                break;
            case R.id.tscbarcode:
                printBarcode();
                break;
            case R.id.tscread:
                printerRead();
                break;
            case R.id.tscpic:
                printPic();
                break;
        }

    }

    /*
    打印图片
     */
    private void printPic() {
        Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent,0);
    }


    /*
    //读取打印机发送到缓存的环形队列里的数据，前提是，你已经开启了读取打印机数据的方法，

     */
    private void printerRead() {
    }

    /*
    打印文本直线，条码
     */
    private void printContent() {
        MainActivity.binder.writeDataByYouself(new UiExecute() {
            @Override
            public void onsucess() {
                showSnackbar("print ok !");

            }

            @Override
            public void onfailed() {
                showSnackbar("print not ok !");

            }
        }, new ProcessData() {
            @Override
            public List<byte[]> processDataBeforeSend() {
                //初始化一个list
                ArrayList<byte[]> list=new ArrayList<byte[]>();
                //在打印请可以先设置打印内容的字符编码类型，默认为gbk，请选择打印机可识别的类型，参看编程手册，打印代码页
                //不设置，默认为gbk
                DataForSendToPrinterTSC.setCharsetName("gbk");
                //通过工具类得到一个指令的byte[]数据,以文本为例
                //首先得设置size标签尺寸,宽60mm,高30mm,也可以调用以dot或inch为单位的方法具体换算参考编程手册
                byte[] data= DataForSendToPrinterTSC.sizeBymm(60,30);
                list.add(data);
                //设置Gap,同上
                list.add(DataForSendToPrinterTSC.gapBymm(0,0));
                //清除缓存
                list.add(DataForSendToPrinterTSC.cls());
                //条码指令，参数：int x，x方向打印起始点；int y，y方向打印起始点；
                //string font，字体类型；int rotation，旋转角度；
                //int x_multiplication，字体x方向放大倍数
                //int y_multiplication,y方向放大倍数
                //string content，打印内容
                byte[] data1 = DataForSendToPrinterTSC
                        .text(10, 10, "0", 0, 1, 1,
                                "abc123");
                list.add(data1);
                //打印直线,int x;int y;int width,线的宽度，int height,线的高度
                list.add(DataForSendToPrinterTSC.bar(20,
                        40, 200, 3));
                //打印条码
                list.add(DataForSendToPrinterTSC.barCode(
                        60, 50, "128", 100, 1, 0, 2, 2,
                        "abcdef12345"));
                //打印
                list.add(DataForSendToPrinterTSC.print(1));
                showSnackbar("content");

                return list;
            }
        });

    }
    /*
    打印文本
     */
    private void printText(){
        MainActivity.binder.writeDataByYouself(new UiExecute() {
            @Override
            public void onsucess() {

            }

            @Override
            public void onfailed() {

            }
        }, new ProcessData() {
            @Override
            public List<byte[]> processDataBeforeSend() {
                //此处用binder里的另外一个发生数据的方法,同样，也要按照编程手册上的示例一样，先设置标签大小
                //如果数据处理较为复杂，请勿选择此方法
                //上面的发送方法的数据处理是在工作线程中完成的，不会阻塞UI线程
                byte[] data0=DataForSendToPrinterTSC.sizeBydot(480, 240);
                byte[] data1=DataForSendToPrinterTSC.cls();

                byte[] data2=DataForSendToPrinterTSC.text(10, 10, "TSS24.BF2", 0, 2, 2, getString(R.string.this_is_text));
                byte[] data3=DataForSendToPrinterTSC.print(1);
                byte[] data= StringUtils.byteMerger(StringUtils.byteMerger
                        (StringUtils.byteMerger(data0, data1), data2), data3);
                List<byte[]> l =new ArrayList<byte[]>();
                l.add(data);
                return l;
            }
        });
    }

    /*
    打印条码
     */
    private void printBarcode(){
        MainActivity.binder.writeDataByYouself(new UiExecute() {
            @Override
            public void onsucess() {

            }

            @Override
            public void onfailed() {

            }
        }, new ProcessData() {
            @Override
            public List<byte[]> processDataBeforeSend() {
                ArrayList<byte[]> list=new ArrayList<byte[]>();
                //通过工具类得到一个指令的byte[]数据,以文本为例
                //首先得设置size标签尺寸,宽60mm,高30mm,也可以调用以dot或inch为单位的方法具体换算参考编程手册
                byte[] data0=DataForSendToPrinterTSC.sizeBymm(60,30);
                list.add(data0);
                //设置Gap,同上
                list.add(DataForSendToPrinterTSC.gapBymm(0,0));
                //清除缓存
                list.add(DataForSendToPrinterTSC.cls());
                //打印条码
                list.add(DataForSendToPrinterTSC.barCode(60,50,"128",100,1,0,2,2,"abcdef12345"));
                //打印
                list.add(DataForSendToPrinterTSC.print(1));

                return list;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("test",requestCode+"  "+resultCode);
        if (requestCode==0&&resultCode==RESULT_OK){
            try{
                Uri imagepath=data.getData();
                ContentResolver resolver = getContentResolver();
                Bitmap b= MediaStore.Images.Media.getBitmap(resolver,imagepath);
                imageView.setImageBitmap(b);
                printpicCode(b);

            }catch (Exception e){
                e.printStackTrace();
                Log.e("pic",e.toString());
            }
        }

    }
    /*
    转成打印机可以识别的code
     */
    private void printpicCode(final Bitmap b) {
        MainActivity.binder.writeDataByYouself(new UiExecute() {
            @Override
            public void onsucess() {
                relativeLayout.setVisibility(View.VISIBLE);
                imageView.setImageBitmap(b);

            }

            @Override
            public void onfailed() {

            }
        }, new ProcessData() {
            @Override
            public List<byte[]> processDataBeforeSend() {
                ArrayList<byte[]> list=new ArrayList<byte[]>();
                list.add(DataForSendToPrinterTSC.bitmap(10, 10, 0,
                        b, BitmapToByteData.BmpType.Threshold));
                list.add(DataForSendToPrinterTSC.print(1));
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
