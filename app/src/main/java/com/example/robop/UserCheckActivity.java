package com.example.robop;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceAuthenticationProvider;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.http.NextServiceFilterCallback;
import com.microsoft.windowsazure.mobileservices.http.OkHttpClientFactory;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilter;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterRequest;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncContext;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.ColumnDataType;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.MobileServiceLocalStoreException;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.SQLiteLocalStore;
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.SimpleSyncHandler;
import com.squareup.okhttp.OkHttpClient;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.microsoft.windowsazure.mobileservices.table.query.QueryOperations.val;

public class UserCheckActivity extends Activity {

    private MobileServiceClient mClient;

    private MobileServiceTable<keyManager> mkeyTable;

    private UserCheckAdapter mAdapter;

    TextView userId;

    TextView userMaster;

    String UserName;

    int allDataNum;
    public static ArrayList<keyManager> datas;


    public void addTest(View v){
        //createTable(UserName);
        searchDate(UserName);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_check);

        try {
            // Create the Mobile Service Client instance, using the provided

            // Mobile Service URL and key
            mClient = new MobileServiceClient(
                    "https://robop-keymanager.azurewebsites.net",
                    this).withFilter(new UserCheckActivity.ProgressFilter());

            // Extend timeout from default of 10s to 20s
            mClient.setAndroidHttpClientFactory(new OkHttpClientFactory() {
                @Override
                public OkHttpClient createOkHttpClient() {
                    OkHttpClient client = new OkHttpClient();
                    client.setReadTimeout(20, TimeUnit.SECONDS);
                    client.setWriteTimeout(20, TimeUnit.SECONDS);
                    return client;
                }
            });

            // Get the Mobile Service Table instance to use

            mkeyTable = mClient.getTable(keyManager.class);

            // Offline Sync
            //mToDoTable = mClient.getSyncTable("ToDoItem", ToDoItem.class);

            //Init local storage
            initLocalStore().get();

            userId=(TextView)findViewById(R.id.id);
            userMaster=(TextView)findViewById(R.id.master);

            mAdapter = new UserCheckAdapter(this, R.layout.row_list_to_do);

            UserCheckActivity.datas=new ArrayList<>();

            //こいつで認証処理を行っている
            //TODO 認証処理を行う前にrefreshItemsFromTableを呼ばない
            authenticate();


            // Load the items from the Mobile Service
            refreshItemsFromTable();



        } catch (MalformedURLException e) {
            createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error");
        } catch (Exception e){
            createAndShowDialog(e, "oncreError");
        }

    }

    private List<keyManager> refreshItemsFromMobileServiceTable() throws ExecutionException, InterruptedException {
        return mkeyTable.where().field("complete").
                eq(val(false)).execute().get();
    }

    /**
     * Initialize local storage
     * @return
     * @throws MobileServiceLocalStoreException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private AsyncTask<Void, Void, Void> initLocalStore() throws MobileServiceLocalStoreException, ExecutionException, InterruptedException {

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {

                    MobileServiceSyncContext syncContext = mClient.getSyncContext();

                    if (syncContext.isInitialized())
                        return null;

                    SQLiteLocalStore localStore = new SQLiteLocalStore(mClient.getContext(), "OfflineStore", null, 1);

                    Map<String, ColumnDataType> tableDefinition = new HashMap<String, ColumnDataType>();
                    tableDefinition.put("id", ColumnDataType.String);
                    tableDefinition.put("master", ColumnDataType.Boolean);

                    localStore.defineTable("keyManager", tableDefinition);

                    SimpleSyncHandler handler = new SimpleSyncHandler();

                    syncContext.initialize(localStore, handler).get();

                } catch (final Exception e) {
                    createAndShowDialogFromTask(e, "backError");
                }

                return null;
            }
        };

        return runAsyncTask(task);
    }

    /**errorMessageDialog関連**/
    private void createAndShowDialogFromTask(final Exception exception, String title) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                createAndShowDialog(exception, "crediError");
            }
        });
    }

    private void createAndShowDialog(Exception exception, String title) {
        Throwable ex = exception;
        if(exception.getCause() != null){
            ex = exception.getCause();
        }
        createAndShowDialog(ex.getMessage(), title);
    }

    private void createAndShowDialog(final String message, final String title) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(message);
        builder.setTitle(title);
        builder.create().show();

    }
    /*********************************/

    private void refreshItemsFromTable() {

        // Get the items that weren't marked as completed and add them in the
        // adapter

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {

                try {
                    final List<keyManager> results = refreshItemsFromMobileServiceTable();

                    //Offline Sync
                    //final List<ToDoItem> results = refreshItemsFromMobileServiceTableSyncTable();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            UserCheckActivity.datas.clear();

                            allDataNum=0;
                            //mAdapterに全データが入ってるので後は煮るなり焼くなりご自由に
                            for (keyManager item : results) {
                                //mAdapter.add(item);
                                UserCheckActivity.datas.add(item);
                                Log.d("abcdef", String.valueOf(datas.get(0).getId()));
                                allDataNum++;
                            }
                        }
                    });
                } catch (final Exception e){
                    createAndShowDialogFromTask(e, "azureError");
                }
                mainThreadUI();
                return null;
            }
        };

        runAsyncTask(task);
    }
    private void mainThreadUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                searchDate(UserName);
            }
        });
    }


    private AsyncTask<Void, Void, Void> runAsyncTask(AsyncTask<Void, Void, Void> task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            return task.execute();
        }
    }

    private class ProgressFilter implements ServiceFilter {

        @Override
        public ListenableFuture<ServiceFilterResponse> handleRequest(ServiceFilterRequest request, NextServiceFilterCallback nextServiceFilterCallback) {

            final SettableFuture<ServiceFilterResponse> resultFuture = SettableFuture.create();

            ListenableFuture<ServiceFilterResponse> future = nextServiceFilterCallback.onNext(request);

            Futures.addCallback(future, new FutureCallback<ServiceFilterResponse>() {
                @Override
                public void onFailure(Throwable e) {
                    resultFuture.setException(e);
                }

                @Override
                public void onSuccess(ServiceFilterResponse response) {
                    resultFuture.set(response);
                }
            });

            return resultFuture;
        }
    }

    private void authenticate() {
        // Login using the Google provider.

        ListenableFuture<MobileServiceUser> mLogin = mClient.login(MobileServiceAuthenticationProvider.Twitter);

        Futures.addCallback(mLogin, new FutureCallback<MobileServiceUser>() {
            @Override
            public void onFailure(Throwable exc) {
                createAndShowDialog((Exception) exc, "conectError");
            }
            @Override
            public void onSuccess(MobileServiceUser user) {
                UserName = user.getUserId();
                createAndShowDialog(String.format(
                        "You are now logged in - %1$2s",
                        user.getUserId()), "Success");
                createTable(user.getUserId());

            }
        });
    }

    //nameでtable作成
    private void createTable(String name) {

        mkeyTable=mClient.getTable(keyManager.class);

        //class型つくる
        keyManager wantItem=new keyManager();

        //てすと
        wantItem.setMaster(false);
        wantItem.setKeyFlag(true);
        wantItem.setId(name);

        //更新+追加
        mkeyTable.insert(wantItem);
        mkeyTable.update(wantItem);

        // Get the table instance to use.
        mkeyTable = mClient.getTable(keyManager.class);

        userId=(TextView)findViewById(R.id.id);
        userMaster=(TextView)findViewById(R.id.master);

        userId.setText(name);
        //searchDate(name);

        // Load the items from Azure.
        refreshItemsFromTable();
    }

    //検索検索～♪
    public void searchDate(String id){

        //TODO ここはdatasの数を取得する処理を書いてfor文を回すほうがスマートです
        for(int i=0;i<allDataNum;i++) {
            if (datas.get(i).getId().equals(id)) {
                if (datas.get(i).isMaster()) {
                    userMaster.setText("masterです");
                    //masterページにIntent
                    Intent intent = new Intent();
                    intent.setClass(this,MasterMainActivity.class);
                    //intent.putExtra("userName",UserName);
                    startActivity(intent);

                } else {
                    userMaster.setText("userです");
                    //userにIntent
                    Intent intent = new Intent();
                    intent.setClass(this,UserMainActivity.class);
                    intent.putExtra("userName",UserName);
                    startActivity(intent);
                }
            }
        }

    }
}
