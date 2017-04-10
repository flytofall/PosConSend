package com.xprinter.posconsend.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;

import com.xprinter.posconsend.R;
import com.xprinter.posconsend.utils.Conts;
import com.xprinter.posconsend.utils.DeviceReceiver;

import net.posprinter.posprinterface.IMyBinder;
import net.posprinter.posprinterface.ProcessData;
import net.posprinter.posprinterface.UiExecute;
import net.posprinter.service.PosprinterService;
import net.posprinter.utils.DataForSendToPrinterPos80;
import net.posprinter.utils.DataForSendToPrinterTSC;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    //IMyBinder接口，所有可供调用的连接和发送数据的方法都封装在这个接口内
    public static IMyBinder binder;

    //bindService的参数connection
    ServiceConnection conn= new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //绑定成功
            binder= (IMyBinder) iBinder;
            Log.e("binder","connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e("disbinder","disconnected");
        }
    };

    public static boolean ISCONNECT;//判断是否连接成功
    Button BTCon,//连接按钮
            BTDisconnect,//断开 按钮
            BTpos,
            BT76,
            BTtsc,
            BtposPrinter,
            BtSb;// 跳转到pos 按钮
    Spinner conPort;//选择连接的方式
    EditText showET;//显示
    CoordinatorLayout container;//提示包容器

    private View dialogView;
    BluetoothAdapter bluetoothAdapter;

    private ArrayAdapter<String> adapter1//蓝牙已配的的adapter
            ,adapter2//蓝牙为配对的adapter
            ,adapter3;//usb的adapter
    private ListView lv1,lv2,lv_usb;
    private ArrayList<String> deviceList_bonded=new ArrayList<String>();//已绑定过的list
    private ArrayList<String> deviceList_found=new ArrayList<String>();//新找到的list
    private Button btn_scan;//蓝牙设备弹窗的“搜索”
    private LinearLayout LLlayout;
    AlertDialog dialog;//弹窗
    String mac;

    private DeviceReceiver myDevice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        //绑定service，获取ImyBinder对象
        Intent intent=new Intent(this,PosprinterService.class);
        bindService(intent, conn, BIND_AUTO_CREATE);
        //初始化控件
        initView();

        //setlistener
        setlistener();
    }
    //初始化控件
    private void initView(){

        BTCon= (Button) findViewById(R.id.buttonConnect);
        BTDisconnect= (Button) findViewById(R.id.buttonDisconnect);

        BTpos= (Button) findViewById(R.id.buttonpos);
        BT76= (Button) findViewById(R.id.button76);
        BTtsc= (Button) findViewById(R.id.buttonTsc);

        BtposPrinter= (Button) findViewById(R.id.buttonPosPrinter);

        BtSb= (Button) findViewById(R.id.buttonSB);
        conPort= (Spinner) findViewById(R.id.connectport);
        showET= (EditText) findViewById(R.id.showET);
        container= (CoordinatorLayout) findViewById(R.id.container);
    }

    int pos ;

    //给按钮添加监听事件
    private void setlistener(){
        BTCon.setOnClickListener(this);
        BTDisconnect.setOnClickListener(this);

        BTpos.setOnClickListener(this);
        BT76.setOnClickListener(this);
        BTtsc.setOnClickListener(this);

        BtSb.setOnClickListener(this);
        conPort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                pos=i;
                switch (i){
                    case 0:
                        //spiner是网络连接时的处理
                        showET.setText("");
                        showET.setEnabled(true);
                        BtSb.setVisibility(View.GONE);
                        showET.setHint(getString(R.string.hint));
                        break;
                    case 1:
                        //spiner是蓝牙连接时的处理
                        showET.setText("");
                        BtSb.setVisibility(View.VISIBLE);
                        showET.setHint(getString(R.string.bleselect));
                        showET.setEnabled(false);
                        break;
                    case 2:
                        //spiner是USB时的处理
                        showET.setText("");
                        BtSb.setVisibility(View.VISIBLE);
                        showET.setHint(getString(R.string.usbselect));
                        showET.setEnabled(false);
                        break;
                    default:break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    @Override
    public void onClick(View view) {

        int id=view.getId();
        //点击连接按钮的处理
        //连接按钮
        if (id== R.id.buttonConnect){
            switch (pos){
                //判断是哪种连接
                case 0:
                    //网络
                    connetNet();
                    break;
                case 1:
                    //蓝牙
                    connetBle();
                    break;
                case 2:
                    break;
            }
        }
        //设备按钮
        if (id== R.id.buttonSB){
            switch (pos){
                case 0:
                    BTCon.setText(getString(R.string.connect));
                    break;
                case 1:
                    setBluetooth();
                    BTCon.setText(getString(R.string.connect));
                    break;
                case 2:
                    BTCon.setText(getString(R.string.connect));
                    break;
            }

        }
        //断开按钮
        if (id== R.id.buttonDisconnect){
            if (ISCONNECT){
                binder.disconnectCurrentPort(new UiExecute() {
                    @Override
                    public void onsucess() {
                        showSnackbar(getString(R.string.toast_discon_success));
                        showET.setText("");
                        BTCon.setText(getString(R.string.connect));
                    }

                    @Override
                    public void onfailed() {
                        showSnackbar(getString(R.string.toast_discon_faile));

                    }
                });
            }else {
                showSnackbar(getString(R.string.toast_present_con));
            }
        }
        //跳到pos
        if (id== R.id.buttonpos){
            if (ISCONNECT){
                Intent intent=new Intent(this,PosActivity.class);
                intent.putExtra("isconnect",ISCONNECT);
                startActivity(intent);
            }else {
                showSnackbar(getString(R.string.connect_first));
            }

        }
        //跳到76打印机
        if (id== R.id.button76){
            if (ISCONNECT){
                Intent intent=new Intent(this,Z76Activity.class);
                intent.putExtra("isconnect",ISCONNECT);
                startActivity(intent);
            }else {
                showSnackbar(getString(R.string.connect_first));
            }
        }
        //跳到条码打印机
        if (id== R.id.buttonTsc){
            if (ISCONNECT){
                Intent intent=new Intent(this,TscActivity.class);
                intent.putExtra("isconnect",ISCONNECT);
                startActivity(intent);
            }else {
                showSnackbar(getString(R.string.connect_first));
            }
        }


    }


    /*
    网络连接
     */
    private void connetNet(){
        //示例：连接打印机网口，参数为：（string）ip地址，（int）端口号，和一个实现的UiExecute接口对象
        //这个接口的实现在连接过程结束后执行（执行于UI线程），onsucess里执行连接成功的代码，onfailed反之；
        //判断是否输入了ip地址
        String ipAddress=showET.getText().toString();
        if (ipAddress.equals(null)||ipAddress.equals("")){

            showSnackbar(getString(R.string.none_ipaddress));
        }else {
            binder.connectNetPort(ipAddress,9100, new UiExecute() {
                @Override
                public void onsucess() {
                    //连接成功后的操作和UI执行
                    ISCONNECT=true;
                    showSnackbar(getString(R.string.con_success));
                    //此处也可以开启读取打印机的数据
                    //参数同样是一个实现的UiExecute接口对象
                    //如果读的过程重出现异常，可以判断连接也发生异常，已经断开
                    //这个读取的方法中，会一直在一条子线程中执行读取打印机发生的数据，
                    //直到连接断开或异常才结束，并执行onfailed
                    binder.acceptdatafromprinter(new UiExecute() {
                        @Override
                        public void onsucess() {

                        }

                        @Override
                        public void onfailed() {
                            ISCONNECT=false;
                            showSnackbar(getString(R.string.con_failed));

                        }
                    });
                }

                @Override
                public void onfailed() {
                    //连接失败后在UI线程中的执行
                    ISCONNECT=false;
                    showSnackbar(getString(R.string.con_failed));
                   BTCon.setText(getString(R.string.con_failed));


                }
            });

        }

    }

    /*
   蓝牙连接
     */
    private void connetBle(){
        String bleAdrress=showET.getText().toString();
        if (bleAdrress.equals(null)||bleAdrress.equals("")){
            showSnackbar(getString(R.string.bleselect));
        }else {
            binder.connectBtPort(bleAdrress, new UiExecute() {
                @Override
                public void onsucess() {
                    ISCONNECT=true;
                    showSnackbar(getString(R.string.con_success));
                    BTCon.setText(getString(R.string.con_success));
                    //此处也可以开启读取打印机的数据
                    //参数同样是一个实现的UiExecute接口对象
                    //如果读的过程重出现异常，可以判断连接也发生异常，已经断开
                    //这个读取的方法中，会一直在一条子线程中执行读取打印机发生的数据，
                    //直到连接断开或异常才结束，并执行onfailed
                    binder.write(DataForSendToPrinterPos80.openOrCloseAutoReturnPrintState(0x1f), new UiExecute() {
                        @Override
                        public void onsucess() {
                                binder.acceptdatafromprinter(new UiExecute() {
                                    @Override
                                    public void onsucess() {

                                    }

                                    @Override
                                    public void onfailed() {
                                        ISCONNECT=false;
                                        showSnackbar(getString(R.string.con_has_discon));
                                    }
                                });
                        }

                        @Override
                        public void onfailed() {

                        }
                    });


                }

                @Override
                public void onfailed() {
                    //连接失败后在UI线程中的执行
                    ISCONNECT=false;
                    showSnackbar(getString(R.string.con_failed));
                }
            });
        }


    }

    /*
    选择蓝牙设备
     */

    public void setBluetooth(){
        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        //判断时候打开蓝牙设备
        if (!bluetoothAdapter.isEnabled()){
            //请求用户开启
            Intent intent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, Conts.ENABLE_BLUETOOTH);
        }else {

            showblueboothlist();

        }
    }

    private void showblueboothlist() {
        if (!bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.startDiscovery();
        }
        LayoutInflater inflater=LayoutInflater.from(this);
        dialogView=inflater.inflate(R.layout.printer_list, null);
        adapter1=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, deviceList_bonded);
        lv1=(ListView) dialogView.findViewById(R.id.listView1);
        btn_scan=(Button) dialogView.findViewById(R.id.btn_scan);
        LLlayout=(LinearLayout) dialogView.findViewById(R.id.ll1);
        lv2=(ListView) dialogView.findViewById(R.id.listView2);
        adapter2=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, deviceList_found);
        lv1.setAdapter(adapter1);
        lv2.setAdapter(adapter2);
        dialog=new AlertDialog.Builder(this).setTitle("BLE").setView(dialogView).create();
        dialog.show();

        myDevice=new DeviceReceiver(deviceList_found,adapter2,lv2);

        //注册蓝牙广播接收者
        IntentFilter filterStart=new IntentFilter(BluetoothDevice.ACTION_FOUND);
        IntentFilter filterEnd=new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(myDevice, filterStart);
        registerReceiver(myDevice, filterEnd);

        setDlistener();
        findAvalibleDevice();
    }
    private void setDlistener() {
        // TODO Auto-generated method stub
        btn_scan.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                LLlayout.setVisibility(View.VISIBLE);
                //btn_scan.setVisibility(View.GONE);
            }
        });
        //已配对的设备的点击连接
        lv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                // TODO Auto-generated method stub
                try {
                    if(bluetoothAdapter!=null&&bluetoothAdapter.isDiscovering()){
                        bluetoothAdapter.cancelDiscovery();

                    }

                    String msg=deviceList_bonded.get(arg2);
                    mac=msg.substring(msg.length()-17);
                    String name=msg.substring(0, msg.length()-18);
                    //lv1.setSelection(arg2);
                    dialog.cancel();
                    showET.setText(mac);
                    //Log.i("TAG", "mac="+mac);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        //未配对的设备，点击，配对，再连接
        lv2.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                // TODO Auto-generated method stub
                try {
                    if(bluetoothAdapter!=null&&bluetoothAdapter.isDiscovering()){
                        bluetoothAdapter.cancelDiscovery();

                    }
                    String msg=deviceList_found.get(arg2);
                    mac=msg.substring(msg.length()-17);
                    String name=msg.substring(0, msg.length()-18);
                    //lv2.setSelection(arg2);
                    dialog.cancel();
                    showET.setText(mac);
                    Log.i("TAG", "mac="+mac);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    /*
    找可连接的蓝牙设备
     */
    private void findAvalibleDevice() {
        // TODO Auto-generated method stub
        //获取可配对蓝牙设备
        Set<BluetoothDevice> device=bluetoothAdapter.getBondedDevices();

        deviceList_bonded.clear();
        if(bluetoothAdapter!=null&&bluetoothAdapter.isDiscovering()){
            adapter1.notifyDataSetChanged();
        }
        if(device.size()>0){
            //存在已经配对过的蓝牙设备
            for(Iterator<BluetoothDevice> it = device.iterator(); it.hasNext();){
                BluetoothDevice btd=it.next();
                deviceList_bonded.add(btd.getName()+'\n'+btd.getAddress());
                adapter1.notifyDataSetChanged();
            }
        }else{  //不存在已经配对过的蓝牙设备
            deviceList_bonded.add("No can be matched to use bluetooth");
            adapter1.notifyDataSetChanged();
        }

    }

    /**
     * 打印文本直线，条码
     * @param ISCONNECT
     */
    private void printTest1(boolean ISCONNECT){

        if (ISCONNECT){
            //向打印机发生打印指令和打印数据，调用此方法
            //第一个参数，还是UiExecute接口的实现，分别是发生数据成功和失败后在ui线程的处理
            //第二个参数是ProcessData接口的实现
            //这个接口的重写processDataBeforeSend这个处理你要发送的指令
            binder.writeDataByYouself(new UiExecute() {
                @Override
                public void onsucess() {
                    showSnackbar(getString(R.string.send_success));
                }

                @Override
                public void onfailed() {
                    showSnackbar(getString(R.string.send_failed));
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

        }else {
            showSnackbar(getString(R.string.toast_present_con));
        }

    }

    /**
     * 打印单行文本
     * @param ISCONNECT
     */
    private void printText(boolean ISCONNECT){
        if(ISCONNECT){
            //此处用binder里的另外一个发生数据的方法,同样，也要按照编程手册上的示例一样，先设置标签大小
            //如果数据处理较为复杂，请勿选择此方法
            //上面的发送方法的数据处理是在工作线程中完成的，不会阻塞UI线程



        }else {
            showSnackbar(getString(R.string.toast_present_con));
        }

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
