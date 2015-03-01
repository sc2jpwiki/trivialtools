package controllers

import play.api._
import play.api.mvc._
import models.Player
import play.api.data.Form
import play.api.data.Forms.{mapping, number, nonEmptyText}
import play.api.i18n.Messages

object Application extends Controller {

  def index(toolname: String) = Action {
    toolname match { 
      case "index" => Ok(views.html.index("can u help me please"))
      case "roulette" => Ok(views.html.roulette("Roulette Tool"))
      case "lapack" => Ok(views.html.mystream("Lapack's Stream and chat page"))
      case _ => Ok(views.html.index("Your new application is ready."))
    }
  }

}

object Players extends Controller {
  private val playerForm: Form[Player] = Form(
    mapping(
      "name" -> nonEmptyText,
      "raceId" -> number,
      "rating" -> number,
      "password" -> nonEmptyText
    )(Player.apply)(Player.unapply)
)
  def list = Action { implicit request =>

    val players = Player.findAll

    Ok(views.html.rating.players(players))
  }
  
  def save = Action { implicit request =>
    val newPlayerForm = playerForm.bindFromRequest()

    newPlayerForm.fold(

      hasErrors = { form =>
        Redirect(routes.Players.newPlayer())
      },

      success = { newPlayer =>
        Player.add(newPlayer)
        Redirect(routes.Players.list())
      }
    )
  }

  def newPlayer = Action { implicit request =>
    val form = if (flash.get("error").isDefined)
      playerForm.bind(flash.data)
    else
      playerForm

    Ok(views.html.rating.newplayer(form))
    }

}


