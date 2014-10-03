package com.stephane.castrec.services;

import com.google.gson.Gson;

import com.stephane.castrec.R;
import com.stephane.castrec.model.OAuthToken;
import com.stephane.castrec.session.OAuthSession;
import com.stephane.castrec.utils.Constants;

import org.apache.http.HttpStatus;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Stéphane Castrec.
 * In charge of getting OAuth Token and storing it in {@Link com.stephane.castrec.session.Session}
 */
public class AuthenticationAsyncTask extends AbstractHttpAsyncTask {

  /** username : Oltu required field to get token. */
  public static final String USERNAME = "username";
  /** password : Oltu required field to get token. */
  public static final String PASSWORD = "password";
  /** client_id : Oltu required field to get token. */
  public static final String CLIENT_ID = "client_id";
  /** client_secret : Oltu required field to get token. */
  public static final String CLIENT_SECRET = "client_secret";
  /** grant_type : Oltu required field to get token. */
  public static final String GRANT_TYPE = "grant_type";
  /** redirect_uri : Oltu required field to get token. */
  public static final String REDIRECT_URI = "redirect_uri";
  /** code : Oltu required field to get token. */
  public static final String CODE = "code";

  /** user pwd. */
  private String pwd;
  /** user login. */
  private String login;

  /**
   * Ctor.
   * Using URL at {@Link R.string.url_authenttication}
   * @param context
   * @param listener
   */
  public AuthenticationAsyncTask(final Context context, final AsyncTaskCommunication listener, final String login, final String pwd) {
    super(context, context.getString(R.string.url_get_oauthtoken), listener);
    this.login = login;
    this.pwd = pwd;
  }

  @Override
  protected Integer doInBackground(final Void... voids) {
    Map<String, String> httpParams = new HashMap<String, String>();
    httpParams.put(USERNAME, login);
    httpParams.put(PASSWORD, pwd);
    httpParams.put(CLIENT_ID, context.getString(R.string.client_id));
    httpParams.put(CLIENT_SECRET, context.getString(R.string.client_secret));
    httpParams.put(GRANT_TYPE, context.getString(R.string.authorization_code));
    httpParams.put(REDIRECT_URI, "not_available");
    httpParams.put(CODE, "not_available");

    int status = 400;
    status = doRestfulRequest(HttpMethod.POST, url, httpParams);

    if(status == HttpStatus.SC_OK){
      OAuthSession.getInstance().setState(Constants.STATE.CONNECTED);
      OAuthSession.getInstance().setOAuthToken(new Gson().fromJson(getResponse(), OAuthToken.class));
    }

    return status;
  }
}
