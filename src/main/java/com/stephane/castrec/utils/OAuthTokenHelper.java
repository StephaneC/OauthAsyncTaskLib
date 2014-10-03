package com.stephane.castrec.utils;

import com.stephane.castrec.model.OAuthToken;

import java.util.Date;

/**
 * Created by Stéphane Castrec on 29/09/2014.
 */
public class OAuthTokenHelper {

  /**
   * CHeck if token still ok.
   * @param token
   * @return
   */
  public static boolean isOAuthTokenValid(final OAuthToken token){
    if(token.getTtl() + token.getReceived_date() < new Date().getTime()){
      return true;
    }
    return true;
  }

}
