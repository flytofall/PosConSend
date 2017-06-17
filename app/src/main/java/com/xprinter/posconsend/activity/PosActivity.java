package com.xprinter.posconsend.activity;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xprinter.posconsend.R;
import com.xprinter.posconsend.utils.StringUtils;
import com.zxy.tiny.Tiny;
import com.zxy.tiny.callback.BitmapCallback;

import net.posprinter.posprinterface.ProcessData;
import net.posprinter.posprinterface.UiExecute;
import net.posprinter.utils.BitmapProcess;
import net.posprinter.utils.BitmapToByteData;
import net.posprinter.utils.DataForSendToPrinterPos58;
import net.posprinter.utils.DataForSendToPrinterPos80;
import net.posprinter.utils.PosPrinterDev;


import java.util.ArrayList;
import java.util.List;


public class PosActivity extends AppCompatActivity implements View.OnClickListener{

    Button btText,btBarCode,btImage,btQRcode,checklink;
    CoordinatorLayout container;
    ImageView imageView;
    EditText text;
    RelativeLayout rl;
    Receiver netReciever;
    TextView tip;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pos);


        netReciever=new Receiver();
        registerReceiver(netReciever,new IntentFilter(MainActivity.DISCONNECT));

        //初始化控件
        initview();
        //判断是否连接
        if (MainActivity.ISCONNECT){
            setListener();
        }else {
            showSnackbar(getString(R.string.con_has_discon));
        }
        Tiny.getInstance().init(getApplication());
    }

    private void initview(){
        container= (CoordinatorLayout) findViewById(R.id.activity_pos);
        btText= (Button) findViewById(R.id.btText);
        btBarCode= (Button) findViewById(R.id.btbarcode);
        btImage= (Button) findViewById(R.id.btpic);
        btQRcode= (Button) findViewById(R.id.qrcode);
        imageView= (ImageView) findViewById(R.id.image);
        rl= (RelativeLayout) findViewById(R.id.rl);
        text= (EditText) findViewById(R.id.text);
        checklink= (Button) findViewById(R.id.checklink);
        tip= (TextView) findViewById(R.id.tv_net_disconnect);
    }

    private void setListener(){
        btText.setOnClickListener(this);
        btBarCode.setOnClickListener(this);
        btImage.setOnClickListener(this);
        btQRcode.setOnClickListener(this);
        checklink.setOnClickListener(this);
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
//                startpostAC();
                break;
            case R.id.btpic:
                printPIC();
                break;
            case R.id.checklink:
                checklink();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(netReciever);
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

                        String str=text.getText().toString();
                        if (str.equals(null)||str.equals("")){
                            showSnackbar(getString(R.string.text_for));
                        }else {
                            //初始化打印机，清除缓存
//                            list.add( DataForSendToPrinterPos58.initializePrinter());
                            list.add(DataForSendToPrinterPos80.initializePrinter());
                            byte[] data1= StringUtils.strTobytes(str);
                            list.add(data1);
                            //追加一个打印换行指令，因为，pos打印机满一行才打印，不足一行，不打印
                            list.add(DataForSendToPrinterPos80.printAndFeedLine());
                            //打印并切纸
//                            list.add(DataForSendToPrinterPos80.selectCutPagerModerAndCutPager(66,1));
                            return list;
                        }
                        return null;
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
                //选择对齐方式
                list.add(DataForSendToPrinterPos80.selectAlignment(1));

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
                b1=convertGreyImg(b);
                Message message=new Message();
                message.what=1;
                handler.handleMessage(message);

                //压缩图片
                Tiny.BitmapCompressOptions options = new Tiny.BitmapCompressOptions();
                Tiny.getInstance().source(b1).asBitmap().withOptions(options).compress(new BitmapCallback() {
                    @Override
                    public void callback(boolean isSuccess, Bitmap bitmap) {
                        if (isSuccess){
//                            Toast.makeText(PosActivity.this,"bitmap: "+bitmap.getByteCount(),Toast.LENGTH_LONG).show();
                            b2=bitmap;
//                            b2=resizeImage(b1,380,false);
                            Message message=new Message();
                            message.what=2;
                            handler.handleMessage(message);
                        }


                    }
                });
//                b2=resizeImage(b1,576,386,false);//576是80型号
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    /*
    bitmap转成打印机可以识别的指令
     */
    private Bitmap b1;//灰度图
    private  Bitmap b2;//压缩图
    private void printpicCode(final Bitmap printBmp){


        MainActivity.binder.writeDataByYouself(new UiExecute() {
            @Override
            public void onsucess() {

            }

            @Override
            public void onfailed() {
                showSnackbar("failed");
            }
        }, new ProcessData() {
            @Override
            public List<byte[]> processDataBeforeSend() {
                List<byte[]> list=new ArrayList<byte[]>();
                list.add(DataForSendToPrinterPos80.initializePrinter());
                list.add(DataForSendToPrinterPos80.printRasterBmp(
                        0,printBmp, BitmapToByteData.BmpType.Threshold, BitmapToByteData.AlignType.Left,576));
//                list.add(DataForSendToPrinterPos80.printAndFeedForward(3));
                list.add(DataForSendToPrinterPos80.selectCutPagerModerAndCutPager(66,1));
                return list;
            }
        });




    }
/*
usb接口打印图片
 */
    private void printUSBbitamp(final Bitmap printBmp){

        int height=printBmp.getHeight();
        //判断图片的高度是否大于200，大于切割图片
        if (height>200){

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
                    list.add(DataForSendToPrinterPos80.initializePrinter());
                    List<Bitmap> bitmaplist=new ArrayList<>();
                    bitmaplist=cutBitmap(200,printBmp);//等高切割图片
                    if(bitmaplist.size()!=0){
                        for (int i=0;i<bitmaplist.size();i++){
                            list.add(DataForSendToPrinterPos80.printRasterBmp(0,bitmaplist.get(i),BitmapToByteData.BmpType.Threshold,BitmapToByteData.AlignType.Center,576));
                        }
                    }
                    list.add(DataForSendToPrinterPos80.selectCutPagerModerAndCutPager(66,1));
                    return list;
                }
            });
        }else {
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
                    list.add(DataForSendToPrinterPos80.initializePrinter());
                    list.add(DataForSendToPrinterPos80.printRasterBmp(
                            0,printBmp, BitmapToByteData.BmpType.Threshold, BitmapToByteData.AlignType.Center,576));
                    list.add(DataForSendToPrinterPos80.selectCutPagerModerAndCutPager(66,1));
                    return list;
                }
            });
        }

    }
    /*
    切割图片方法，等高切割
     */
    private List<Bitmap> cutBitmap(int h,Bitmap bitmap){
        int width=bitmap.getWidth();
        int height=bitmap.getHeight();
        boolean full=height%h==0;
        int n=height%h==0?height/h:(height/h)+1;
        Bitmap b;
        List<Bitmap> bitmaps=new ArrayList<>();
        for (int i=0;i<n;i++){
            if (full){
                b=Bitmap.createBitmap(bitmap,0,i*h,width,h);
            }else {
                if (i==n-1){
                    b=Bitmap.createBitmap(bitmap,0,i*h,width,height-i*h);
                }else {
                    b=Bitmap.createBitmap(bitmap,0,i*h,width,h);
                }
            }

            bitmaps.add(b);
        }

        return bitmaps;
    }




    /**
     * 显示提示信息
     * @param showstring 显示的内容字符串
     */
    private void showSnackbar(String showstring){
        Snackbar.make(container, showstring,Snackbar.LENGTH_LONG)
                .setActionTextColor(getResources().getColor(R.color.button_unable)).show();
    }

   public Handler handler=new Handler(){
       @Override
       public void handleMessage(Message msg) {
           super.handleMessage(msg);
           switch (msg.what){
               case 1:
                   rl.setVisibility(View.VISIBLE);
                   tip.setVisibility(View.GONE);
                   imageView.setImageBitmap(b1);
                   break;
               case 2:
                   //判断接口类型，usb需要特殊处理
                   if (PosPrinterDev.PortType.USB!=MainActivity.portType){
                       printpicCode(b2);
                   }else {
                       printUSBbitamp(b2);
                   }



                   tip.setVisibility(View.GONE);
                   break;
               case 3://断开连接
                   btText.setEnabled(false);
                   btBarCode.setEnabled(false);
                   btQRcode.setEnabled(false);
                   btImage.setEnabled(false);
                   tip.setVisibility(View.VISIBLE);
                   break;
               case 4:
                   tip.setVisibility(View.VISIBLE);
                   break;


           }

       }
   };
    private void startpostAC(){
        Intent intent =new Intent(this,QRcodeActivity.class);
        startActivity(intent);
    }

    /**
     * 二值法的到的单色图
     * 灰度图,再转为单色图，的到单色图的图像信息
     * @param img 位图
     * @return  data返回转换好的单色位图的图像信息
     */
    public Bitmap convertGreyImg(Bitmap img) {
        int width = img.getWidth();         //获取位图的宽
        int height = img.getHeight();       //获取位图的高

        int[] pixels = new int[width * height]; //通过位图的大小创建像素点数组

        img.getPixels(pixels, 0, width, 0, 0, width, height);


        //求灰度图的的算术平均值，阈值
        double redSum=0,greenSum=0,blueSun=0;
        double total=width*height;

        for(int i = 0; i < height; i++)  {
            for(int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];

                int red = ((grey  & 0x00FF0000 ) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);



                redSum+=red;
                greenSum+=green;
                blueSun+=blue;


            }
        }
        int m=(int) (redSum/total);

        //二值法，转换单色图
        for(int i = 0; i < height; i++)  {
            for(int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];

                int alpha1 = 0xFF << 24;
                int red = ((grey  & 0x00FF0000 ) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);


                if (red>=m) {
                    red=green=blue=255;
                }else{
                    red=green=blue=0;
                }
                grey = alpha1 | (red << 16) | (green << 8) | blue;
                pixels[width*i+j]=grey;


            }
        }
        Bitmap mBitmap=Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        mBitmap.setPixels(pixels, 0, width, 0, 0, width, height);



        return mBitmap;
    }



    /*
    检查连接
     */
    private void checklink(){
        MainActivity.binder.checkLinkedState(new UiExecute() {
            @Override
            public void onsucess() {
                showSnackbar("连接未断开");
            }

            @Override
            public void onfailed() {
                showSnackbar("连接已断开");
                Message message =new Message();
                message.what=3;
                handler.handleMessage(message);

            }
        });
    }
/*
广播接收
 */
    private class Receiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if (action.equals(MainActivity.DISCONNECT)){
                Message message=new Message();
                message.what=4;
                handler.handleMessage(message);
            }
        }
    }
}
