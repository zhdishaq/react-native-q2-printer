package io.q2.printer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;

import com.iposprinter.iposprinterservice.*;

import java.util.ArrayList;

import io.q2.printer.Util.*;
import io.q2.printer.model.BarCode;
import io.q2.printer.model.BlankLines;
import io.q2.printer.model.FormatText;
import io.q2.printer.model.PImage;
import io.q2.printer.model.QRCode;
import io.q2.printer.model.SimpleText;
import io.q2.printer.model.Table;

public class CBQ2PrinterManager extends ReactContextBaseJavaModule {

    private static ReactApplicationContext reactContext;

    public static final String REACT_CLASS = "CBQ2Printer";




    private static final String TAG                 = "IPosPrinter";


    private static final String VERSION        = "V1.1.0";


    private final int PRINTER_NORMAL = 0;
    private final int PRINTER_PAPERLESS = 1;
    private final int PRINTER_THP_HIGH_TEMPERATURE = 2;
    private final int PRINTER_MOTOR_HIGH_TEMPERATURE = 3;
    private final int PRINTER_IS_BUSY = 4;
    private final int PRINTER_ERROR_UNKNOWN = 5;


    private int printerStatus = 0;


 private final ArrayList<Object> objs;
    private final String  PRINTER_NORMAL_ACTION = "com.iposprinter.iposprinterservice.NORMAL_ACTION";
    private final String  PRINTER_PAPERLESS_ACTION = "com.iposprinter.iposprinterservice.PAPERLESS_ACTION";
    private final String  PRINTER_PAPEREXISTS_ACTION = "com.iposprinter.iposprinterservice.PAPEREXISTS_ACTION";
    private final String  PRINTER_THP_HIGHTEMP_ACTION = "com.iposprinter.iposprinterservice.THP_HIGHTEMP_ACTION";
    private final String  PRINTER_THP_NORMALTEMP_ACTION = "com.iposprinter.iposprinterservice.THP_NORMALTEMP_ACTION";
    private final String  PRINTER_MOTOR_HIGHTEMP_ACTION = "com.iposprinter.iposprinterservice.MOTOR_HIGHTEMP_ACTION";
    private final String  PRINTER_BUSY_ACTION = "com.iposprinter.iposprinterservice.BUSY_ACTION";
    private final String  PRINTER_CURRENT_TASK_PRINT_COMPLETE_ACTION = "com.iposprinter.iposprinterservice.CURRENT_TASK_PRINT_COMPLETE_ACTION";



    private final int MSG_TEST                               = 1;
    private final int MSG_IS_NORMAL                          = 2;
    private final int MSG_IS_BUSY                            = 3;
    private final int MSG_PAPER_LESS                         = 4;
    private final int MSG_PAPER_EXISTS                       = 5;
    private final int MSG_THP_HIGH_TEMP                      = 6;
    private final int MSG_THP_TEMP_NORMAL                    = 7;
    private final int MSG_MOTOR_HIGH_TEMP                    = 8;
    private final int MSG_MOTOR_HIGH_TEMP_INIT_PRINTER       = 9;
    private final int MSG_CURRENT_TASK_PRINT_COMPLETE     = 10;


    private final int  MULTI_THREAD_LOOP_PRINT  = 1;
    private final int  INPUT_CONTENT_LOOP_PRINT = 2;
    private final int  DEMO_LOOP_PRINT          = 3;
    private final int  PRINT_DRIVER_ERROR_TEST  = 4;
    private final int  DEFAULT_LOOP_PRINT       = 0;


    private       int  loopPrintFlag            = DEFAULT_LOOP_PRINT;
    private       byte loopContent              = 0x00;
    private       int  printDriverTestCount     = 0;

    private IPosPrinterService mIPosPrinterService;
    private IPosPrinterCallback callback = null;

    private HandlerUtils.MyHandler handler;



    private BroadcastReceiver IPosPrinterStatusListener = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent){
            String action = intent.getAction();
            if(action == null)
            {
                Log.d(TAG,"IPosPrinterStatusListener onReceive action = null");
                return;
            }
            Log.d(TAG,"IPosPrinterStatusListener action = "+action);
            if(action.equals(PRINTER_NORMAL_ACTION))
            {
                handler.sendEmptyMessageDelayed(MSG_IS_NORMAL,0);
            }
            else if (action.equals(PRINTER_PAPERLESS_ACTION))
            {
                handler.sendEmptyMessageDelayed(MSG_PAPER_LESS,0);
            }
            else if (action.equals(PRINTER_BUSY_ACTION))
            {
                handler.sendEmptyMessageDelayed(MSG_IS_BUSY,0);
            }
            else if (action.equals(PRINTER_PAPEREXISTS_ACTION))
            {
                handler.sendEmptyMessageDelayed(MSG_PAPER_EXISTS,0);
            }
            else if (action.equals(PRINTER_THP_HIGHTEMP_ACTION))
            {
                handler.sendEmptyMessageDelayed(MSG_THP_HIGH_TEMP,0);
            }
            else if (action.equals(PRINTER_THP_NORMALTEMP_ACTION))
            {
                handler.sendEmptyMessageDelayed(MSG_THP_TEMP_NORMAL,0);
            }
            else if (action.equals(PRINTER_MOTOR_HIGHTEMP_ACTION))  //此时当前任务会继续打印，完成当前任务后，请等待2分钟以上时间，继续下一个打印任务
            {
                handler.sendEmptyMessageDelayed(MSG_MOTOR_HIGH_TEMP,0);
            }
            else if(action.equals(PRINTER_CURRENT_TASK_PRINT_COMPLETE_ACTION))
            {
                handler.sendEmptyMessageDelayed(MSG_CURRENT_TASK_PRINT_COMPLETE,0);
            }
            else
            {
                handler.sendEmptyMessageDelayed(MSG_TEST,0);
            }
        }
    };
    private ServiceConnection connectService = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIPosPrinterService = IPosPrinterService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIPosPrinterService = null;
        }
    };
    public int getPrinterStatus() {

        Log.i(TAG, "***** printerStatus" + printerStatus);
        try {
            printerStatus = mIPosPrinterService.getPrinterStatus();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "#### printerStatus" + printerStatus);
        return printerStatus;
    }


    public void printerInit() {
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    mIPosPrinterService.printerInit(callback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private HandlerUtils.IHandlerIntent iHandlerIntent = new HandlerUtils.IHandlerIntent()
    {
        @Override
        public void handlerIntent(Message msg)
        {
            switch (msg.what) {
                case MSG_TEST:
                    break;

                case MSG_IS_BUSY:
                    Toast.makeText(getReactApplicationContext(), "The printer is working", Toast.LENGTH_SHORT).show();
                    break;
                case MSG_PAPER_LESS:
                    loopPrintFlag = DEFAULT_LOOP_PRINT;
                    Toast.makeText(getReactApplicationContext(), "The printer is out of paper", Toast.LENGTH_SHORT).show();
                    break;
                case MSG_PAPER_EXISTS:
                    Toast.makeText(getReactApplicationContext(), "The printer paper is ready", Toast.LENGTH_SHORT).show();
                    break;
                case MSG_THP_HIGH_TEMP:
                    Toast.makeText(getReactApplicationContext(), "Printer high temperature alarm", Toast.LENGTH_SHORT).show();
                    break;
                case MSG_MOTOR_HIGH_TEMP:
                    loopPrintFlag = DEFAULT_LOOP_PRINT;
                    Toast.makeText(getReactApplicationContext(),"Motor high temperature alarm", Toast.LENGTH_SHORT).show();
                    handler.sendEmptyMessageDelayed(MSG_MOTOR_HIGH_TEMP_INIT_PRINTER, 180000);  //马达高温报警，等待3分钟后复位打印机
                    break;
                case MSG_MOTOR_HIGH_TEMP_INIT_PRINTER:
                    printerInit();
                    break;
                case MSG_CURRENT_TASK_PRINT_COMPLETE:
                    Toast.makeText(getReactApplicationContext(), "The print task has been printed", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };


    CBQ2PrinterManager(ReactApplicationContext context) {
        super(context);
        reactContext = context;





        Intent intent=new Intent();
        intent.setPackage("com.iposprinter.iposprinterservice");
        intent.setAction("com.iposprinter.iposprinterservice.IPosPrintService");
        //startService(intent);
        reactContext. bindService(intent, connectService, Context.BIND_AUTO_CREATE);



        IntentFilter printerStatusFilter = new IntentFilter();
        printerStatusFilter.addAction(PRINTER_NORMAL_ACTION);
        printerStatusFilter.addAction(PRINTER_PAPERLESS_ACTION);
        printerStatusFilter.addAction(PRINTER_PAPEREXISTS_ACTION);
        printerStatusFilter.addAction(PRINTER_THP_HIGHTEMP_ACTION);
        printerStatusFilter.addAction(PRINTER_THP_NORMALTEMP_ACTION);
        printerStatusFilter.addAction(PRINTER_MOTOR_HIGHTEMP_ACTION);
        printerStatusFilter.addAction(PRINTER_BUSY_ACTION);

        reactContext.registerReceiver(IPosPrinterStatusListener,printerStatusFilter);


        objs = new ArrayList<Object>();


    }

    @ReactMethod
    public void init() {

        objs.clear();
        if (handler == null) {
            handler = new HandlerUtils.MyHandler(iHandlerIntent);
        }

        if (callback == null) {
            callback = new IPosPrinterCallback.Stub() {
                @Override
                public void onRunResult(boolean isSuccess) throws RemoteException {


                }

                @Override
                public void onReturnString(String result) throws RemoteException {

                }
            };
        }

    }
    @ReactMethod
    public void addImage(final ReadableMap readableMap) {
        PImage img=new PImage();
        img.setImg(readableMap.getString("img"));
        img.setAlign(readableMap.getInt("align"));
        img.setSize(readableMap.getInt("size"));
        objs.add(img);
    }
    @ReactMethod
    public void printImage(final ReadableMap readableMap)
    {



        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try{


                    byte[] decodedString = Base64.decode(readableMap.getString("img"), Base64.DEFAULT);
                    Bitmap mBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    mIPosPrinterService.printBitmap(readableMap.getInt("align"),readableMap.getInt("size"),mBitmap,callback);
                    mIPosPrinterService.printerPerformPrint(100,  callback);
                }catch (RemoteException e){
                    e.printStackTrace();
                }
            }
        });
    }

    @ReactMethod
    public void addFormatText(final String text, final int fontSize,final int align)
    {

       FormatText t=new FormatText();
       t.setAlign(align);
       t.setSize(fontSize);
       t.setText(text);
       objs.add(t);
    }
    @ReactMethod
    public void printFormatText(final String text, final int fontSize,final int align)
    {

        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try{

               mIPosPrinterService.PrintSpecFormatText(text,"ST",fontSize,align,callback);
               mIPosPrinterService.printerPerformPrint(100,  callback);
                }catch (RemoteException e){
                    e.printStackTrace();
                }
            }
        });
    }

    @ReactMethod
    public void addText(final String text)
    {
        SimpleText t=new SimpleText();
        t.setText(text);
        objs.add(t);
    }
    @ReactMethod
    public void printText(final String text)
    {

        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try{

                    mIPosPrinterService.printText(text,callback);
                    mIPosPrinterService.printerPerformPrint(100,  callback);
                }catch (RemoteException e){
                    e.printStackTrace();
                }
            }
        });
    }

    @ReactMethod
    public void addBlankLine(final int lines, final int height)
    {

       BlankLines b=new BlankLines();
       b.setHeight(height);
       b.setLines(lines);
       objs.add(b);
    }
    @ReactMethod
    public void printBlankLine(final int lines, final int height)
    {

        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try{

                    mIPosPrinterService.printBlankLines(lines,height,callback);
                    mIPosPrinterService.printerPerformPrint(100,  callback);
                }catch (RemoteException e){
                    e.printStackTrace();
                }
            }
        });
    }

    @ReactMethod
    public void addQRCode(final String text,final int modulesize,final int mErrorCorrectionLevel)
    {
        QRCode q=new QRCode();
        q.setmErrorCorrectionLevel(mErrorCorrectionLevel);
        q.setModulesize(modulesize);
        q.setText(text);
        objs.add(q);

    }


    @ReactMethod
    public void printQRCode(final String text,final int modulesize,final int mErrorCorrectionLevel)
    {

        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try{
                    mIPosPrinterService.printQRCode(text,modulesize,mErrorCorrectionLevel,callback);
                    mIPosPrinterService.printerPerformPrint(100,  callback);
                }catch (RemoteException e){
                    e.printStackTrace();
                }
            }
        });
    }
    @ReactMethod
    public void addBarCode(final String text,final int symbology,final int height,final int width,final int align)
    {
        BarCode b=new BarCode();
        b.setAlign(align);
        b.setHeight(height);
        b.setSymbology(symbology);
        b.setText(text);
        objs.add(b);
    }
    @ReactMethod
    public void printBarCode(final String text,final int symbology,final int height,final int width,final int align)
    {

        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try{
                    mIPosPrinterService.printBarCode(text, symbology, height, width, align, callback);
                    mIPosPrinterService.printerPerformPrint(100,  callback);
                }catch (RemoteException e){
                    e.printStackTrace();
                }
            }
        });
    }

    @ReactMethod
    public void addTable(final ReadableMap table,final ReadableArray width,final ReadableArray align,final int fontsize,final int tablealign) {

        Table t=new Table();
        int[] widths=new int[width.size()];
        for (int i=0; i<width.size();i++){
            widths[i]=width.getInt(i);
        }
        int[] aligns=new int[align.size()];
        for (int i=0; i<align.size();i++){
            aligns[i]=align.getInt(i);
        }
        t.setColumnalign(aligns);
        t.setColumnwidth(widths);
        t.setRows(table.getArray("rows"));
        t.setTablealign(tablealign);
        t.setFontsize(fontsize);

        objs.add(t);

    }

    @ReactMethod
    public void print() {

        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try{

                    for (Object item : objs) {
                        switch (item.toString()){
                            case "Table":
                                int isContinuous=1;
                                Table t =(Table) item;
                                mIPosPrinterService.setPrinterPrintAlignment(t.getTablealign(), callback);
                                mIPosPrinterService.setPrinterPrintFontSize(t.getFontsize(), callback);
                                for (int i=0;i<t.getRows().size();i++) {
                                    ReadableArray text = t.getRows().getArray(i);
                                    String[] texts = new String[text.size()];
                                    for (int ii = 0; ii < text.size(); ii++) {
                                        texts[ii] = text.getString(ii);
                                    }

                                    if (i == t.getRows().size() - 1) {
                                        isContinuous = 0;
                                    }
                                    mIPosPrinterService.printColumnsText(texts, t.getColumnwidth(), t.getColumnalign(), isContinuous, callback);

                                }
                                break;
                            case "FormatText":
                                FormatText ft=(FormatText)item;
                                mIPosPrinterService.PrintSpecFormatText(ft.getText(),"ST",ft.getSize(),ft.getAlign(),callback);
                                break;
                            case "BarCode":
                                BarCode b=(BarCode)item;
                                mIPosPrinterService.printBarCode(b.getText(), b.getSymbology(), b.getHeight(), b.getWidth(), b.getAlign(), callback);
                                break;
                            case "QRCode":
                                QRCode q=(QRCode) item;
                                mIPosPrinterService.printQRCode(q.getText(), q.getModulesize(), q.getmErrorCorrectionLevel(), callback);
                                break;
                            case "BlankLines":
                                BlankLines bl=(BlankLines) item;
                                mIPosPrinterService.printBlankLines(bl.getLines(),bl.getHeight(),callback);
                                break;
                            case "SimpleText":
                                SimpleText st=(SimpleText) item;
                                mIPosPrinterService.printText(st.getText(),callback);
                                break;
                            case "Image":
                                PImage img=(PImage) item;
                                byte[] decodedString = Base64.decode(img.getImg(), Base64.DEFAULT);
                                Bitmap mBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                mIPosPrinterService.printBitmap(img.getAlign(),img.getSize(),mBitmap,callback);
                                break;


                        }

                    }

                    mIPosPrinterService.printerPerformPrint(0,  callback);
                    objs.clear();

                }catch (RemoteException e){
                    e.printStackTrace();
                }
            }
        });
    }

        @ReactMethod
    public void printTable(final ReadableMap table,final ReadableArray width,final ReadableArray align,final int fontsize,final int tablealign){
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try{

                    int[] widths=new int[width.size()];
                    for (int i=0; i<width.size();i++){
                        widths[i]=width.getInt(i);
                    }
                    int[] aligns=new int[align.size()];
                    for (int i=0; i<align.size();i++){
                        aligns[i]=align.getInt(i);
                    }
                    int isContinuous=1;
                     mIPosPrinterService.setPrinterPrintAlignment(tablealign, callback);
                     mIPosPrinterService.setPrinterPrintFontSize(fontsize, callback);
                     for (int i=0;i<table.getArray("rows").size();i++){
                         ReadableArray text=table.getArray("rows").getArray(i);
                         String[] texts=new String[text.size()];
                         for (int ii=0; ii<text.size();ii++){
                             texts[ii]=text.getString(ii);
                         }

                      if(i==table.getArray("rows").size()-1){
                          isContinuous=0;
                      }
                         mIPosPrinterService.printColumnsText(texts, widths, aligns, isContinuous, callback);

                     }

                     mIPosPrinterService.printerPerformPrint(100,  callback);

                }catch (RemoteException e){
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }


}
