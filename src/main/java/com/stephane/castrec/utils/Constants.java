package com.stephane.castrec.utils;

/**
 * @author Stéphane Castrec
 */
public class Constants {

  public static final String TAG = "OAuthLib";

  public static final int ERROR_NO_INTERNET = 1000;

  /**
   * User connection state.
   */
  public enum STATE {
    /**
     * No OAuth Token available.
     */
    DISCONNECTED,
    /**
     * OAuthToken available and up to date.
     */
    CONNECTED,
    /**
     * OAuthToken available, but TTL ended.
     */
    TO_REFRESH
  }

  ;

}
