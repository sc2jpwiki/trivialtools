package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {

  def index(toolname: String) = Action {
    toolname match { 
      case "index" => Ok(views.html.index("Your new application is ready."))
      case"roulette" => Ok(views.html.roulette("Roulette Tool"))
      case _ => Ok(views.html.index("Your new application is ready."))
    }
  }

}
