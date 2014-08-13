package controllers

import play.api.Play.current
import play.api.libs.json.{JsArray, JsValue, Json}
import play.api.libs.ws.WS
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by bcarlson on 8/5/14.
 */
object TwitterClient extends Controller with TwitterAuth {
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
        val json = Json.parse(response.body)
        val tweets = (json \ "statuses").as[JsArray].value.
          map(simplifyResponse)

        Ok(views.html.twittersearch.render(query,
          tweets.map(tweet =>
            s"${tweet \ "tweet"} - ${tweet \ "name"}")))
      }
    }.getOrElse {
      Future.successful(InternalServerError("Please configure twitter authentication using the twitter.conf file"))
    }
  }

  def simplifyResponse(tweetResponse: JsValue): JsValue = Json.obj(
    "name" -> tweetResponse \ "user" \ "name",
    "account" -> tweetResponse \ "user" \ "screen_name",
    "tweet" -> tweetResponse \ "text"
  )


}
