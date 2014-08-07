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
  this: Controller =>

  def encode(s: String): String = URLEncoder.encode(s, "UTF-8")
  def value(key: String): Option[String] = current.configuration.getString(key).map(encode)
  
  val apiKey = value("twitter.consumerKey")
  val apiSecret = value("twitter.consumerSecret")

  val accessToken = value("twitter.accessToken")
  val accessSecret = value("twitter.accessSecret")

  val consumerKey = for {
    key <- apiKey
    secret <- apiSecret
  } yield ConsumerKey(key, secret)

  val requestToken = for {
    token <- accessToken
    secret <- accessSecret
  } yield RequestToken(token, secret)

  val auth = for {
    key <- consumerKey
    token <- requestToken
  } yield OAuthCalculator(key, token)
}
