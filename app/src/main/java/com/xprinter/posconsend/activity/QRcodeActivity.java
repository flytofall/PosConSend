package com.xprinter.posconsend.activity;

import android.graphics.Bitmap;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.google.zxing.common.BitmapUtils;
import com.xprinter.posconsend.R;

import net.posprinter.posprinterface.ProcessData;
import net.posprinter.posprinterface.UiExecute;
import net.posprinter.utils.DataForSendToPrinterPos80;

import java.util.ArrayList;
import java.util.List;

/*
二维码界面
 */
public class QRcodeActivity extends AppCompatActivity implements View.OnClickListener{

    CoordinatorLayout container;
    Spinner error_level;
    ImageView qrcode;
    EditText contenttext;
    Button btclear,btcreat,btprint,btscan;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);
        //初始化控件
        initView();
    }

    private void initView() {
        container= (CoordinatorLayout) findViewById(R.id.activity_qrcode);
        error_level= (Spinner) findViewById(R.id.errorlevel);
        qrcode= (ImageView) findViewById(R.id.qrcode);
        contenttext= (EditText) findViewById(R.id.content);
        btclear= (Button) findViewById(R.id.bt_qrclear);
        btcreat= (Button) findViewById(R.id.bt_qrcreat);
        btprint= (Button) findViewById(R.id.bt_qrprint);
        btscan= (Button) findViewById(R.id.bt_qrscan);

        //设置监听
        btclear.setOnClickListener(this);
        btcreat.setOnClickListener(this);
        btprint.setOnClickListener(this);
        btscan.setOnClickListener(this);
//      spiner设置监听
        Spinnerlisner();
    }

    int errorlevel=7;

    private void Spinnerlisner() {
        error_level.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i){
                    case 0:
                        errorlevel=7;
                        break;
                    case 1:
                        errorlevel=15;
                        break;
                    case 2:
                        errorlevel=25;
                        break;
                    case 3:
                        errorlevel=30;
                        break;
                    default:break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

                errorlevel=7;
            }
        });
    }

    @Override
    public void onClick(View view) {

        int id= view.getId();

        switch (id){
            case  R.id.bt_qrclear:
                clear();
            break;
            case R.id.bt_qrcreat:
                create();
                break;
            case R.id.bt_qrprint:
                print();
                break;
            case R.id.bt_qrscan:
                scan();
        }


    }

    private void scan() {

    }

    //发给打印机打印
    private void print() {
        MainActivity.binder.writeDataByYouself(new UiExecute() {
            @Override
            public void onsucess() {
//                showSnackbar("ok");
            }

            @Override
            public void onfailed() {
                showSnackbar("failed");
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
                list.add(DataForSendToPrinterPos80.SetsTheSizeOfTheQRCodeSymbolModule(4));
                //设置错误级别
                if (errorlevel!=0){
                    list.add(DataForSendToPrinterPos80.SetsTheErrorCorrectionLevelForQRCodeSymbol(errorlevel));
                }else {
                    list.add(DataForSendToPrinterPos80.SetsTheErrorCorrectionLevelForQRCodeSymbol(7));
                }
                //存储二维码的数据到打印机的存储区域
                String qrcontent=contenttext.getText().toString().trim();
                if (qrcontent.length()!=0){
                    list.add(DataForSendToPrinterPos80.StoresSymbolDataInTheQRCodeSymbolStorageArea(qrcontent));
                    //打印存储区域的二维码
                    list.add(DataForSendToPrinterPos80.PrintsTheQRCodeSymbolDataInTheSymbolStorageArea());
                    //打印并换行
                    list.add(DataForSendToPrinterPos80.printAndFeedLine());
                }else {
                    showSnackbar(getString(R.string.none_content));
                }

                return list;
            }
        });
    }

    //创造二维码
    private void create() {
        String str=contenttext.getText().toString();
        if (str.equals(null)&&str.equals("")||str.length()==0){
            showSnackbar(getString(R.string.qrcode_content));
        }else {

            Bitmap bitmap= null;
            str=str.trim();
            try{
                bitmap=BitmapUtils.create2DCode(str);
                qrcode.setBackgroundResource(0);
                qrcode.setImageBitmap(bitmap);
                qrcode.setScaleType(ImageView.ScaleType.FIT_XY );

            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    //清除打印内容
    private void clear() {
        contenttext.setText("");
        contenttext.setHint(getString(R.string.qrcode_content));

    }

    //提示消息

    private void showSnackbar(String showstring){
        Snackbar.make(container, showstring,Snackbar.LENGTH_LONG)
                .setActionTextColor(getResources().getColor(R.color.button_unable)).show();
    }

}
