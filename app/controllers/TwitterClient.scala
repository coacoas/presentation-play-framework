package controllers

import java.net.URLDecoder

import play.api.{Routes, Logger}
import play.api.Play.current
import play.api.libs.EventSource
import play.api.libs.iteratee.{Enumerator, Concurrent, Enumeratee, Iteratee}
import play.api.libs.json.{JsObject, JsArray, JsValue, Json}
import play.api.libs.ws.WS
import play.api.mvc.{WebSocket, Action, Controller}
import play.core.Router

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by bcarlson on 8/5/14.
 */
object TwitterClient extends Controller with TwitterAuth {

  def index = Action {
    Ok(views.html.twittersearch.render)
  }

  def simplifyResponse(tweetResponse: JsValue): JsValue = Json.obj(
    "name" -> tweetResponse \ "user" \ "name",
    "account" -> tweetResponse \ "user" \ "screen_name",
    "tweet" -> tweetResponse \ "text"
  )

  def search(query: String) = Action.async {
    auth.map { signature =>
      WS.url(s"https://api.twitter.com/1.1/search/tweets.json").
        sign(signature).
        withQueryString(
          "q" -> query,
          "count" -> 20.toString,
          "result_type" -> "mixed",
          "lang" -> "en").
        get.map { response =>
          val tweets =
            (Json.parse(response.body) \ "statuses").as[JsArray].value.map(simplifyResponse)
          Ok(Json.prettyPrint(Json.toJson(tweets)))
        }
    }.getOrElse {
      Future.successful(InternalServerError("Please configure twitter authentication using the twitter.conf file"))
    }
  }

  def filter(query: String) = WebSocket.using[JsValue] { request =>
    auth.map { signature =>
      val (stream, channel) = Concurrent.broadcast[JsValue]
      var chunkCache = ""
      def tweetIteratee = Iteratee.foreach[Array[Byte]] { chunk =>
        val chunkString = new String(chunk, "UTF-8")

        if (chunkCache.isEmpty && !chunkString.startsWith("{")) {
          Logger.info(chunkString)
          channel.push(Json.obj("error" -> chunkString))
          channel.end(new IllegalArgumentException(chunkString))
        } else {
          chunkCache += chunkString
          if (chunkCache.trim.endsWith("}")) {
            channel.push(Json.parse(chunkCache))
            chunkCache = ""
          }
        }
      }

      WS.url(s"https://stream.twitter.com/1.1/statuses/filter.json").
        sign(signature).
        withQueryString("track" -> URLDecoder.decode(query, "UTF-8")).
        postAndRetrieveStream("")(_ => tweetIteratee)

      Logger.info(s"Streaming data for $query")

      val in = Iteratee.ignore[JsValue]

      val out = stream &>
        Concurrent.buffer(100) &>
        Enumeratee.take(100) &>
        Enumeratee.map[JsValue](simplifyResponse)

      (in, out)
    } getOrElse {
      val response: JsValue = Json.obj("error" ->
        "Please configure twitter authentication using the twitter.conf file")
      (Iteratee.ignore[JsValue], Enumerator(response) >>> Enumerator.eof)
    }
  }
}
