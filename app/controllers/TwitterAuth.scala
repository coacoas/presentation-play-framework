package controllers

import java.net.URLEncoder

import play.api.{Play, Configuration}
import play.api.libs.oauth.{ConsumerKey, OAuthCalculator, RequestToken}
import play.api.mvc.Controller
import play.api.Play.current

/**
 * Created by bcarlson on 8/5/14.
 */
trait TwitterAuth {
  def encode(s: String): String = URLEncoder.encode(s, "UTF-8")
  def value(key: String): Option[String] = current.configuration.getString(key).map(encode)
  
  private [this] val consumerKey = for {
    key <- value("twitter.consumerKey")
    secret <- value("twitter.consumerSecret")
  } yield ConsumerKey(key, secret)

  private [this] val requestToken = for {
    token <- value("twitter.accessToken")
    secret <- value("twitter.accessSecret")
  } yield RequestToken(token, secret)

  val auth = for {
    key <- consumerKey
    token <- requestToken
  } yield OAuthCalculator(key, token)
}
