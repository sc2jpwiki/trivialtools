package controllers

import play.api._
import play.api.mvc._
import models.Player
import models.Report
import models.ReportedGame
import models.GameStatus
import play.api.data.Form
import play.api.data.Forms.{mapping, number, longNumber, nonEmptyText}
import play.api.i18n.Messages

import org.squeryl.adapters.H2Adapter
import org.squeryl.{Session, SessionFactory}
import play.api.db.DB
import play.api.{Application, GlobalSettings}
import org.joda.time.DateTime
import org.joda.time.format._

import org.squeryl.KeyedEntity
import org.squeryl.Schema
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Table
import org.squeryl.Query
import collection.Iterable
import org.squeryl.adapters.H2Adapter
import org.squeryl.{Session, SessionFactory}
import play.api.db.DB

import java.sql.Timestamp

import math._

//top page

object Homepage extends Controller {

  def index(toolname: String) = Action {
    toolname match { 
      case "index" => Ok(views.html.index("can u help me please"))
    case "roulette" => Ok(views.html.roulette("Roulette Tool"))
  case "lapack" => Ok(views.html.mystream("Lapack's Stream and chat page"))
case _ => Ok(views.html.index("Your new application is ready."))
    }
  }

}


//elo rating app

object Players extends Controller {

  //new player form
  private val playerForm = Form(
    mapping(
      "name" -> nonEmptyText.verifying("validation.name.duplicate", Player.isNameNotUsed(_)),
      "race" -> nonEmptyText,
      "rating" -> longNumber,
      "password" -> nonEmptyText
    )(Player.apply)(Player.unapply)
)

//game result report form
private val reportForm: Form[Report] = Form(
  mapping(
    "name" -> nonEmptyText,
    "opponent" -> nonEmptyText,
    "win" -> number.verifying("can't be negative number", {_ >= 0}),
  "lose" -> number.verifying("can't be negative number", {_ >= 0}),
    "password" -> nonEmptyText
  )(Report.apply)(Report.unapply)
)

//Go-to ranking page action
  def list = Action { implicit request =>
    val players = Player.findAll
    Ok(views.html.rating.players(players))
  }

//new player registration Action
def save = Action { implicit request =>
val newPlayerForm = playerForm.bindFromRequest()

  newPlayerForm.fold(

    hasErrors = { form =>
      Redirect(routes.Players.newPlayer()).flashing(Flash(form.data) + ("error" -> Messages("validation.errors")))
    },

    success = { newPlayer =>
      Player.add(newPlayer)
      val message = Messages("players.new.success", newPlayer.name)
      Redirect(routes.Players.list()).flashing("success" -> message)
    }
  )
}

//Game result report submission Action

def submit = Action { implicit request =>
  val newReportForm = reportForm.bindFromRequest()

  newReportForm.fold(

  hasErrors = { form =>
  Redirect(routes.Players.report()).flashing(Flash(form.data) + ("error" -> Messages("validation.errors")))
    },

    success = { report =>
    val reporter = Player(report.you, "", 0, report.password)
      val opponent = Player(report.opponent, "", 0, "")

      //TRANSACTION START

    //auth reporter
    if(Player.auth(reporter)) {
      //existense check of opponent
      if(Player.existOrNot(opponent) & reporter.name != opponent.name) {
        //query for players old elo
        transaction {
        val reporterInTable = Player.findByName(reporter.name)
        val opponentInTable = Player.findByName(opponent.name)

        //calculating elo
        val (reporterNewElo, opponentNewElo) = calculateElo(32.0, reporterInTable.rating, opponentInTable.rating, report.win, report.lose)

        //setting values to ReportedGame instance
        val gameTobeReported = ReportedGame(reporterInTable.id, opponentInTable.id, report.win, report.lose, reporterInTable.rating, opponentInTable.rating,
          reporterNewElo, opponentNewElo,  new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()))

        //update players table elo
        Player.updateElo(reporterInTable.name, reporterNewElo)
        Player.updateElo(opponentInTable.name, opponentNewElo)

        //insert to reported games table
        ReportedGame.add(gameTobeReported)

        //commit

        //TRANSACTION END
      }

        val message = Messages("players.report.success")
          Redirect(routes.Players.list()).flashing("success" -> message)


      } else { // opponent doesnt exist or same name as reporter
      val message = Messages("players.report.opponent.error") //to be overwrittern
      Redirect(routes.Players.report()).flashing("error" -> message)//to be overwrittern
    } 
  } else { //if auth failed
    val message = Messages("players.report.reporter.auth.fail") //to be overwrittern
    Redirect(routes.Players.report()).flashing("error" -> message)//to be overwrittern
  }
}
  )

}

//elo calculation function

def calculateElo(k: Double)(reporterOldElo: Long, opponentOldElo: Long, win: Int, lose: Int) = {
  val qReporter = pow(10,reporterOldElo*1.0/400) :Double
  val qOpponent = pow(10, opponentOldElo*1.0/400) :Double
  val qRatio = pow(10, (opponentOldElo-reporterOldElo)*1/400) :Double

  val eReporter = 1.0/(1.0+qRatio) :Double

  val gamesNum = win + lose

  val eloGain = round( k * (win - gamesNum * eReporter)) :Long

  val reporterNewElo = reporterOldElo + eloGain
  val opponentNewElo = opponentOldElo - eloGain
  (reporterNewElo, opponentNewElo)
}

//Go-to game report page Action
def report = Action { implicit request =>
val form = if (flash.get("error").isDefined)
  reportForm.bind(flash.data)
else
  reportForm

Ok(views.html.rating.report(form))
    }


    //Go-to registration page Action
    def newPlayer = Action { implicit request =>
    val form = if (flash.get("error").isDefined)
      playerForm.bind(flash.data)
    else
      playerForm

    Ok(views.html.rating.newplayer(form))
  }

  //Go-to game report page Action
  def newGame = Action { implicit request =>
  val form = if (flash.get("error").isDefined)
    playerForm.bind(flash.data)
  else
    playerForm
  Ok(views.html.rating.newplayer(form))
  }
  
  //Go-to history page Action
  def matchhistory = Action { implicit request =>
  val games = ReportedGame.findAll
  Ok(views.html.rating.history(games))
  }


}

  


