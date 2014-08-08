package controllers

import play.api.Logger
import play.api.Play.current
import play.api.libs.iteratee.{Enumeratee, Iteratee, Enumerator, Concurrent}
import play.api.libs.{EventSource, json}
import play.api.libs.json.{JsValue, JsArray, Json}
import play.api.libs.ws.WS
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by bcarlson on 8/5/14.
 */
object TwitterClient extends Controller with TwitterAuth {
  def index = Action { Ok(views.html.twittersearch.render) }

  def simplifyResponse(tweetResponse: JsValue): JsValue = Json.obj(
    "name" -> tweetResponse \ "user" \ "name",
    "account" -> tweetResponse \ "user" \ "screen_name",
    "tweet" -> tweetResponse \ "text"
  )

  def filter(query: String) = Action {
    auth.map { signature =>
      val (stream, channel) = Concurrent.broadcast[JsValue]
      var chunkCache = ""
      def tweetIteratee = Iteratee.foreach[Array[Byte]] { chunk =>
        val chunkString = new String(chunk, "UTF-8")

        if (chunkString contains "No filter parameters found") {
          channel.push(Json.obj("error" -> chunkString))
          Logger.info(chunkString)
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
        withQueryString("track" -> query).
        postAndRetrieveStream("")(_ => tweetIteratee)

      Ok.feed(stream &>
        Enumeratee.take(100) &>
        Enumeratee.map[JsValue](simplifyResponse)
        &> EventSource()).as("text/event-stream")
    }.getOrElse {
      InternalServerError("Please configure twitter authentication.")
    }
  }
}
