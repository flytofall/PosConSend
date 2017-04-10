package com.xprinter.posconsend.activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
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
import net.posprinter.utils.DataForSendToPrinterPos80;


import java.util.ArrayList;
import java.util.List;


public class PosActivity extends AppCompatActivity implements View.OnClickListener{

    Button btText,btBarCode,btImage,btQRcode;
    CoordinatorLayout container;
    ImageView imageView;
    RelativeLayout rl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pos);

        //初始化控件
        initview();
        //判断是否连接
        if (MainActivity.ISCONNECT){
            setListener();
        }else {
            showSnackbar(getString(R.string.con_has_discon));
        }
    }

    private void initview(){
        container= (CoordinatorLayout) findViewById(R.id.activity_pos);
        btText= (Button) findViewById(R.id.btText);
        btBarCode= (Button) findViewById(R.id.btbarcode);
        btImage= (Button) findViewById(R.id.btpic);
        btQRcode= (Button) findViewById(R.id.qrcode);
        imageView= (ImageView) findViewById(R.id.image);
        rl= (RelativeLayout) findViewById(R.id.rl);
    }

    private void setListener(){
        btText.setOnClickListener(this);
        btBarCode.setOnClickListener(this);
        btImage.setOnClickListener(this);
        btQRcode.setOnClickListener(this);
    }
    @Override
    public void onClick(View view) {
        int id=view.getId();
        switch (id){
            case R.id.btText:
                printText();
                break;
            case R.id.btbarcode:
                printBarcode();
                break;
            case R.id.qrcode:
                printQRcode();
                break;
            case R.id.btpic:
                printPIC();
                break;
        }



    }

    /*
    打印文本
    pos指令中并没有专门的打印文本的指令
    但是，你发送过去的数据，如果不是打印机能识别的指令，满一行后，就可以自动打印了，或者加上OA换行，也能打印
     */
    private void printText(){

        MainActivity.binder.writeDataByYouself(
                new UiExecute() {
                    @Override
                    public void onsucess() {

                    }

                    @Override
                    public void onfailed() {

                    }
                }, new ProcessData() {
                    @Override
                    public List<byte[]> processDataBeforeSend() {

                        List<byte[]> list=new ArrayList<byte[]>();
                        //创建一段我们想打印的文本,转换为byte[]类型，并添加到要发送的数据的集合list中
                        String str = "Welcome to use the impact and thermal printer manufactured " +
                                "by professional POS receipt printer company!";
                        byte[] data1= StringUtils.strTobytes(str);
                        list.add(data1);
                        //追加一个打印换行指令，因为，pos打印机满一行才打印，不足一行，不打印
                        list.add(DataForSendToPrinterPos80.printAndFeedLine());
                        return list;
                    }
                });

    }

    /*
    打印条码
	pos的条码打印和TSC的条码打印不太一样
	你需要在打印条码前，设置好条码的各个属性，如宽，高，HRI等
     */
    private void printBarcode(){
        MainActivity.binder.writeDataByYouself(new UiExecute() {
            @Override
            public void onsucess() {
                showSnackbar("01234567890");
            }

            @Override
            public void onfailed() {

            }
        }, new ProcessData() {
            @Override
            public List<byte[]> processDataBeforeSend() {
                List<byte[]>list=new ArrayList<byte[]>();
                //初始化打印机，清除缓存
                list.add(DataForSendToPrinterPos80.initializePrinter());
                //选择对齐方式
                list.add(DataForSendToPrinterPos80.selectAlignment(1));
                //选择HRI文字的位置,
                list.add(DataForSendToPrinterPos80.selectHRICharacterPrintPosition(02));
                //设置条码宽度,参数单位和意义请参考编程手册
                list.add(DataForSendToPrinterPos80.setBarcodeWidth(3));
                //设置条码高度，一般为162
                list.add(DataForSendToPrinterPos80.setBarcodeHeight(162));
                //打印条码，注意，打印条码有2个方法，俩个方法对应的条码类型不一样，使用需要参考编程手册和方法注解
                //UPC-A
                list.add(DataForSendToPrinterPos80.printBarcode(69,10,"B123456789"));
                //上面的指令只是在flash里绘制了这个条码，打印还需要一个打印指令
                list.add(DataForSendToPrinterPos80.printAndFeedLine());

                return list;
            }
        });
    }
    /*
    打印二维码，同样，打印二维码，也需要做一些打印前的设置
    一些必要的设置，需要参考编程手册给的示例，再调用对应的指令的方法
     */
    private void printQRcode(){
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
                List<byte[]> list=new ArrayList<byte[]>();
                //先初始化打印机，清除缓存
                list.add(DataForSendToPrinterPos80.initializePrinter());

                //指定二维码的模型
                list.add(DataForSendToPrinterPos80.SetsTheSizeOfTheQRCodeSymbolModule(3));
                //设置错误级别
                list.add(DataForSendToPrinterPos80.SetsTheErrorCorrectionLevelForQRCodeSymbol(48));
                //存储二维码的数据到打印机的存储区域
                list.add(DataForSendToPrinterPos80.StoresSymbolDataInTheQRCodeSymbolStorageArea(
                        "Welcome to Printer Technology to create advantages Quality to win in the future"
                ));
                //打印存储区域的二维码
                list.add(DataForSendToPrinterPos80.PrintsTheQRCodeSymbolDataInTheSymbolStorageArea());
                //打印并换行
                list.add(DataForSendToPrinterPos80.printAndFeedLine());
                //或者调用简单的封装过的打印二维码的方法
                //不同的是，调用上面的分步方法，只要缓存里的数据没有清除，
                //调用PrintsTheQRCodeSymbolDataInTheSymbolStorageArea，就可以直接打印，而不用再次设置二维码内容
                //DataForSendToPrinterPos80.printQRcode(3, 48, "www.net")
                //相当于每次都重新设置了缓存里的二维码内容

                //list.add(DataForSendToPrinterPos80.printQRcode(3, 48, "www.xprint.net"));

                return list;
            }
        });

    }

    /*
    打印光栅位图，推荐打印图片使用此方法，这种打印方式可以更好的打印较大的图片，而不受打印机内存限制
    去相册选择图像，在onactivityresult里回调，得到一个bitmap对象，然后调用发送printRasteBmp指令
     */
    private void printPIC(){
        Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent,0);
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
                Bitmap b=MediaStore.Images.Media.getBitmap(resolver,imagepath);
                imageView.setImageBitmap(b);
                printpicCode(b);

            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    /*
    bitmap转成打印机可以识别的指令
     */
    private void printpicCode(final Bitmap bitmaps){

        MainActivity.binder.writeDataByYouself(new UiExecute() {
            @Override
            public void onsucess() {

                rl.setVisibility(View.VISIBLE);
            }

            @Override
            public void onfailed() {
                showSnackbar("failed");
            }
        }, new ProcessData() {
            @Override
            public List<byte[]> processDataBeforeSend() {
                List<byte[]> list=new ArrayList<byte[]>();
                //设置相对打印位置，让图片居中
                int w=bitmaps.getWidth();
                int h=bitmaps.getHeight();
                int x=0;
                if (w<576){//576位80打印机的打印纸的可打印宽度
                    x=(576-w)/2;
                }
                int m=x%256;
                int n=x/256;
                Log.e("test","m="+m+",n="+n);

                list.add(DataForSendToPrinterPos80.printRasterBmp(
                        0,bitmaps, BitmapToByteData.BmpType.Threshold, BitmapToByteData.AlignType.Center,576));
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

   Handler handler=new Handler(){
       @Override
       public void handleMessage(Message msg) {
           super.handleMessage(msg);
       }
   };
}
