package controllers

import play.api.Play.current
import play.api.libs.json.{JsArray, Json}
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
          map(status => (status \ "user" \ "name", status \ "text"))

        Ok(views.html.twittersearch.render(query,
          tweets.map { case (user, tweet) => user.as[String] + ": " + tweet.as[String]}))
      }
    }.getOrElse {
      Future.successful(InternalServerError("Please configure twitter authentication using the twitter.conf file"))
    }
  }
}
