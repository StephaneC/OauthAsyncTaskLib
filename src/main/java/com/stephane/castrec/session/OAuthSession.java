package com.stephane.castrec.session;

import com.stephane.castrec.model.OAuthToken;
import com.stephane.castrec.utils.Constants;
import com.stephane.castrec.utils.OAuthTokenHelper;

import java.util.Date;


/**
 * @author Stéphane Castrec
 *         In charge of retrieving User session information
 *         - User connection state
 *         - OAuth token
 */
public class OAuthSession {

  private static OAuthSession session = null;

  private OAuthSession() {
  }

  /**
   * Get
   */
  public static OAuthSession getInstance() {
    if (session == null) {
      session = new OAuthSession();
    }
    return session;
  }

  /**
   * connection state.
   */
  private Constants.STATE state = Constants.STATE.DISCONNECTED;

  /**
   * OAuthToken.
   */
  private OAuthToken clientToken = null;


  public OAuthToken getOAuthToken() {
    return clientToken;
  }

  public void setOAuthToken(OAuthToken clientToken) {
    this.clientToken = clientToken;
  }

  public Constants.STATE getOAuthState() {
    if (state != Constants.STATE.DISCONNECTED) {
      //DO NOTHING
      if (state == Constants.STATE.CONNECTED) {
        //check if still connected
        if (OAuthTokenHelper.isOAuthTokenValid(getOAuthToken())) {
          //Token expired
          if (getOAuthToken().getRefreshToken() != null) {//TODO use guava nullOrEmpty
            state = Constants.STATE.TO_REFRESH;
          } else {
            state = Constants.STATE.DISCONNECTED;
          }
        }
      }
    }
    return state;
  }

  public void setState(final Constants.STATE state) {
    this.state = state;
  }
}
