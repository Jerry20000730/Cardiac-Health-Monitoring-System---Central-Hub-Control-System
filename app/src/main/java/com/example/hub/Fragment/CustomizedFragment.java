package com.example.hub.Fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.hub.FileOperator.fileOperator;
import com.example.hub.MainActivity;
import com.example.hub.R;
import com.example.hub.Server.serverThread;
import com.example.hub.isInitialBindingSuccess.BindingSuccessObserver;
import com.example.hub.isInitialBindingSuccess.isInitialBindingSuccessObservable;
import com.stephentuso.welcome.WelcomeFinisher;
import com.stephentuso.welcome.WelcomePage;
import com.stephentuso.welcome.WelcomeUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import static java.lang.Thread.sleep;

public class CustomizedFragment extends Fragment implements WelcomePage.OnChangeListener {

    private ViewGroup rootLayout;
    private socketTask sockettask;
    private WelcomeFinisher welcomeFinisher = new WelcomeFinisher(this);
    private static final String HUBIDFILE = "/storage/emulated/0/hubID.txt";
    private static final String SENSORLISTFILE = "/storage/emulated/0/sensors.txt";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_example, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rootLayout = (ViewGroup) view.findViewById(R.id.layout);

        view.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkInternetPermission()) {
                    sockettask = new socketTask();
                    sockettask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        });
    }

    @Override
    public void onWelcomeScreenPageScrolled(int pageIndex, float offset, int offsetPixels) {
        if (rootLayout != null)
            WelcomeUtils.applyParallaxEffect(rootLayout, true, offsetPixels, 0.3f, 0.2f);
    }

    @Override
    public void onWelcomeScreenPageSelected(int pageIndex, int selectedPageIndex) {
        // not used
    }

    @Override
    public void onWelcomeScreenPageScrollStateChanged(int pageIndex, int state) {
        // not used
    }

    private boolean checkInternetPermission() {
        WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(getContext(), "Please switch on Wi-Fi before binding", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public class socketTask extends AsyncTask<String, Integer, Void> {

        private int port;
        private ServerSocket serverSocket;
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            // port for socket communication is set on 7801
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMax(3);
            progressDialog.setMessage("start pairing");
            progressDialog.setCancelable(false);
            getActivity().runOnUiThread(() -> progressDialog.show());
            port = 7801;
        }

        @Override
        protected Void doInBackground(String... strings) {
            System.out.println("[INFO] Server is listening on port: " + port);
            int step = 1;
            Socket socket = null;
            try {
                serverSocket = new ServerSocket(port);
                serverSocket.setSoTimeout(20000);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            try {
                socket = serverSocket.accept();
                serverThread server = new serverThread(socket);
                String returnINFO = server.recv();
                assert returnINFO != null;
                if (!fileOperator.isFileExistAndNonEmpty(SENSORLISTFILE)) {
                    fileOperator.writeData("/storage/emulated/0/", "hubID", "HUB001");
                }
                getActivity().runOnUiThread(() -> progressDialog.setMessage("receive sensor information"));
                publishProgress(step);
                step++;
                if ((int) server.inputHeader.getInstructionCmd() == 1) {
                    System.out.println(returnINFO);
                    if (!fileOperator.isFileExistAndNonEmpty(HUBIDFILE)) {
                        fileOperator.writeData("/storage/emulated/0/", "sensors", returnINFO);
                    } else {
                        fileOperator.deleteFile(SENSORLISTFILE);
                        sleep(200);
                        fileOperator.writeData("/storage/emulated/0/", "sensors", returnINFO.substring(17));
                    }
                    getActivity().runOnUiThread(() -> progressDialog.setMessage("exchange information"));
                    publishProgress(step);
                    step++;
                    server.send(fileOperator.readFromFile(HUBIDFILE));
                    getActivity().runOnUiThread(() -> progressDialog.setMessage("pairing success"));
                    publishProgress(step);
                }
            } catch(SocketTimeoutException e) {
                getActivity().runOnUiThread(() -> progressDialog.dismiss());
                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Binding Process Time Out, Check the Connectivity", Toast.LENGTH_SHORT).show());
            } catch (IOException | NullPointerException ioException) {
                ioException.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            getActivity().runOnUiThread(() -> progressDialog.dismiss());
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
            try {
                if (fileOperator.isFileExistAndNonEmpty(SENSORLISTFILE)) {
                    isInitialBindingSuccessObservable.getInstance().setIsInitialBindingSuccess(true);
                    welcomeFinisher.finish();
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}


