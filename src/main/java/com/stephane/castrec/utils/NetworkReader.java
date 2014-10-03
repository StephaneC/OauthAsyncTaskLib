package com.stephane.castrec.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * @author Stéphane Castrec
 *         In Charge of checking the internet state.
 */
public class NetworkReader {

  private NetworkReader() {
  }

  /**
   *
   * @param context
   * @return
   */
  public static boolean isInternetAvailable(final Context context) {
    try {
      ConnectivityManager cm
        = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

      NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
      return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    } catch (Exception e) {
      Log.e(Constants.TAG, "Error on checking internet:", e);

    }
    //default allowed to access internet
    return true;
  }
}
