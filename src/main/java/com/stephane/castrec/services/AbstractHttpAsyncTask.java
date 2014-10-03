package com.stephane.castrec.services;

import com.google.gson.Gson;

import com.stephane.castrec.R;
import com.stephane.castrec.model.OAuthToken;
import com.stephane.castrec.session.OAuthSession;
import com.stephane.castrec.utils.Constants;
import com.stephane.castrec.utils.NetworkReader;
import com.stephane.castrec.utils.OAuthTokenHelper;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract Async task in charge of calling server.
 *
 * @author Stéphane Castrec
 *         In charge of doing all HTTP request throught oauth.
 */
public abstract class AbstractHttpAsyncTask extends AsyncTask<Void, Void, Integer> {

  /**
   * referer token to pass apis filter.
   */
  private static final String TOKEN = "token";

  /**
   * OAUth Authorization type.
   */
  private static final String AUTHORIZATION = "Authorization";

  /**
   * listener to get AsyncTask Response.
   */
  private AsyncTaskCommunication listener = null;

  /**
   * Context to check internet availability.
   */
  protected Context context;

  /**
   * Url to call.
   */
  protected String url;

  /**
   * oauthAutoRefresh  : define if have to refresh token if needed.
   */
  protected boolean oauthAutoRefresh = false;

  /**
   * Http Method.
   */
  public enum HttpMethod {
    GET, POST, DELETE, PUT,
  }

  /**
   * Interface to define how this AsyncTask could send response back.
   */
  public interface AsyncTaskCommunication {
    /** called when asyncTask succeed. */
    void succeed();
    /** called when asyncTask failed. */
    void error(int httpStatus);
    /** called if no internet. */
    void errorNoInternet();
  }


  /**
   * JSON Response received.
   */
  private String response;

  /**
   * Ctor.
   * Default impl. DO NOT REFRESH OAUTH TOKEN.
   */
  public AbstractHttpAsyncTask(final Context context, final String url, final AsyncTaskCommunication listener) {
    this.context = context;
    this.url = url;
    this.listener = listener;
  }

  /**
   * Ctor:
   * Provide a boolean to specify if this is a specific AUTHENTICATED method.
   */
  public AbstractHttpAsyncTask(final Context context, final String url,
                               final AsyncTaskCommunication listener, final boolean oauthAutoRefresh) {
    this.context = context;
    this.url = url;
    this.listener = listener;
    this.oauthAutoRefresh = oauthAutoRefresh;
  }

  /**
   * Do http request with correct headers and params as entity.
   */
  public int doRestfulRequest(final HttpMethod method, final String url, Map<String, String> params) {

    // first check that internet is connected.
    if (!NetworkReader.isInternetAvailable(context)) {
      return Constants.ERROR_NO_INTERNET;
    }

    try {
      //check if we have to refresh.
      if (oauthAutoRefresh) {
        if (OAuthSession.getInstance().getOAuthState() == Constants.STATE.TO_REFRESH) {
          int status = refreshOAuthToken();
          if(status != HttpStatus.SC_OK){
            //return refresh error
            return status;
          }
        }
      }

      //then create an httpClient.
      HttpClient client = new DefaultHttpClient();
      HttpRequestBase request = getRequest(method);
      request.setURI(URI.create(url));
      setBasicHeaders(request);

      addParams(request, params);

      // do request.
      HttpResponse httpResponse = client.execute(request);

      //Store response
      this.response = EntityUtils.toString(httpResponse.getEntity());

      Log.d(Constants.TAG, "received for url: " + request.getURI() + " return code: " + httpResponse
        .getStatusLine()
        .getStatusCode());
      return httpResponse.getStatusLine().getStatusCode();

    } catch (final UnsupportedEncodingException e) {
      Log.e(Constants.TAG, "" + e);
    } catch (ClientProtocolException e) {
      Log.e(Constants.TAG, "" + e);
    } catch (IOException e) {
      Log.e(Constants.TAG, "" + e);
    }
    return HttpStatus.SC_SERVICE_UNAVAILABLE;
  }

  /**
   * Add params to a Request.
   * WARNING : Request method HAVE to be set.
   * @param request : request to add params.
   * @param params : params to add.
   */
  private void addParams(final HttpRequestBase request, final Map<String, String> params) throws UnsupportedEncodingException {
    if (params != null) {
      //create pair values to send to server
      List<NameValuePair> pairs = formatParams(params);
      //set entity
      if (request.getMethod().equals(HttpMethod.POST.toString())) {
        ((HttpPost) request).setEntity(new UrlEncodedFormEntity(pairs));
      } else {
        if (request.getMethod().equals(HttpMethod.PUT.toString())) {
          ((HttpPut) request).setEntity(new UrlEncodedFormEntity(pairs));
        }
      }
    }
  }

  /**
   * Format parameters.
   */
  private List<NameValuePair> formatParams(final Map<String, String> params) {
    List<NameValuePair> pairs = new ArrayList<NameValuePair>();
    for (Map.Entry<String, String> entry : params.entrySet()) {
      pairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
    }
    return pairs;
  }

  /**
   * Called when doInBackground ended.
   */
  protected void onPostExecute(Integer result) {
    if (listener != null) {
      switch (result) {
        case HttpStatus.SC_OK:
        case HttpStatus.SC_CREATED:
        case HttpStatus.SC_ACCEPTED:
          listener.succeed();
          break;
        case Constants.ERROR_NO_INTERNET:
          listener.errorNoInternet();
          break;
        default:
          listener.error(result);
          break;
      }
    }
  }


  /**
   * In charge of adding basics headers for requests.
   */
  private void setBasicHeaders(final HttpRequestBase request) {
    if (OAuthSession.getInstance().getOAuthState() == Constants.STATE.CONNECTED) {
      request.setHeader(AUTHORIZATION,
                        "Bearer"/*Session.getInstance().getOAuthToken().getTokenType()*/ + " "
                          + OAuthSession.getInstance().getOAuthToken().getAccessToken());
      request.setHeader(TOKEN, OAuthSession.getInstance().getOAuthToken().getAccessToken());
    }
  }

  /**
   * create the correct HttpMethod.
   */
  private HttpRequestBase getRequest(final HttpMethod method) {
    switch (method) {
      case GET:
        return new HttpGet();
      case POST:
        return new HttpPost();
      case DELETE:
        return new HttpDelete();
      case PUT:
        return new HttpPut();
    }
    return null;
  }

  /**
   * refresh oauth token.
   */
  //TODO find a better way to reuse all code un AsbtractAsyncTask.
  //but calling an asynctask from asynctask is BAD
  private int refreshOAuthToken() throws IOException {
    HttpClient client = new DefaultHttpClient();
    HttpRequestBase request = getRequest(HttpMethod.POST);
    request.setURI(URI.create(context.getString(R.string.url_refresh_oauthtoken)));
    setBasicHeaders(request);

    //adding params
    Map<String, String> params = new HashMap<String, String>();
    params.put("refresh_token", OAuthSession.getInstance().getOAuthToken().getRefreshToken());
    addParams(request, params);

    //execute request
    HttpResponse httpResponse = client.execute(request);
    if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
      //Store response
      this.response = EntityUtils.toString(httpResponse.getEntity());
      OAuthSession.getInstance().setOAuthToken(new Gson().fromJson(getResponse(), OAuthToken.class));
    }

    Log.d(Constants.TAG, "[RefreshToken] received for url: " + request.getURI() + " return code: " + httpResponse
      .getStatusLine()
      .getStatusCode());

    return httpResponse.getStatusLine().getStatusCode();
  }

  /**
   * Get http response as String
   */
  public String getResponse() {
    return response;
  }
}
