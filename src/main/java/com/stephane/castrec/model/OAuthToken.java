package com.stephane.castrec.model;

/**
 * @author Stéphane Castrec
 *         Oauth Token model.
 */
public class OAuthToken {

  /**
   * access_token: token value.
   */
  private String access_token;

  /**
   * refresh token :
   * to get a new OAuth token without asking authentication.
   */
  private String refresh_token;

  /**
   * TTL
   */
  private long expires_in;

  private long received_date;

  /**
   * Oauth token type.
   */
  private String token_type;

  public OAuthToken() {
  }


  public String getRefreshToken() {
    return refresh_token;
  }

  public void setRefreshToken(String refreshToken) {
    this.refresh_token = refreshToken;
  }

  public long getTtl() {
    return expires_in;
  }

  public void setTtl(long ttl) {
    this.expires_in = ttl;
  }

  public String getAccessToken() {
    return access_token;
  }

  public void setAccessToken(String accessToken) {
    this.access_token = accessToken;
  }

  public String getTokenType() {
    return token_type;
  }

  public long getReceived_date() {
    return received_date;
  }

  public void setReceived_date(final long received_date) {
    this.received_date = received_date;
  }

  public void setToken_type(final String token_type) {
    this.token_type = token_type;
  }
}
