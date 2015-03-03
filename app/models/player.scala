package models
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

//Definition of Schema

object Database extends Schema {
  val playersTable = table[Player]("players")
  val reportedGamesTable = table[ReportedGame]("reportedgames")

  on(playersTable) { p => declare {
    p.id is(autoIncremented)
  }}

  on(reportedGamesTable) { r => declare {
    r.id is(autoIncremented)
  }}
}

//Models. one-to-one correnspondence to the databale tables

case class Player(
  name: String, 
  race: String, 
  rating: Long, 
  password: String) extends KeyedEntity[Long] {
  val id: Long = 0
}

object Player {
  import Database.playersTable
//mock
  var players = Set (
    Player("Albion", "Protoss",530000,"prpr"),
    Player("PSiArc", "Terran",10000,"t1love"),
    Player("MoMoTaro","Zerg" ,1200,"goji")
  )

//  def findAll = players.toList.sortBy(-_.rating)
  def findAll = inTransaction {
    allQ.toList
  }

//squeryl codes
//select
  def allQ: Query[Player] = from(playersTable) {
    player => select(player) orderBy(player.rating desc)
  }
  
  def byNameQ(name: String): Query[Player] = from(playersTable) {
    player => where(player.name === name) select(player) 
  }


//insert
  def add(player: Player) = inTransaction{
    playersTable.insert(player)
  }

//update

  def updateElo(name: String, newElo: Long) = inTransaction{
    update(playersTable)(p =>
    where(p.name === name)
    set(p.rating := newElo)
    )	
  }

//findByName
  def findByName(name: String): Player = byNameQ(name).toList.head

//isNameUsed
  def isNameNotUsed(name: String) = inTransaction {
    byNameQ(name).toList.isEmpty
  }

//authentication name is supposed to be unique key
  def auth(player: Player):Boolean = inTransaction { 
    if(existOrNot(player)) {
    val playerInTable = byNameQ(player.name).toList.head
    if(player.password == playerInTable.password) {
      true
    } else {
      false
    }
  } else {
    false
  }
}


//existense
def existOrNot(player: Player) = inTransaction {
  !byNameQ(player.name).toList.isEmpty
}
  //exists(from(playersTable)((p) => where(p.name === player.name) select(p.id)))
}

case class ReportedGame(
  reporter: Long, 
  opponent: Long, 
  win: Long, 
  lose: Long, 
  oldRatingReporter: Long, 
  oldRatingOpponent: Long, 
  newRatingReporter: Long, 
  newRatingOpponent: Long, 
  reportedDate: Timestamp, 
  confirmedDate: Timestamp
) extends KeyedEntity[Long] {
  val id: Long = 0
}

object ReportedGame {
  import Database.reportedGamesTable
  import Database.playersTable

  def findAll = inTransaction {
    
    def compareWith7th = (
      (
        x: (String, String, Long, Long, Long, Long, java.sql.Timestamp), 
        y: (String, String, Long, Long, Long, Long, java.sql.Timestamp)
      ) => 
      (x._7).after(y._7)
    )

  allMatchHistory.toList sortWith compareWith7th
  }

//squeryl codes
//select
  def allQ: Query[ReportedGame] = from(reportedGamesTable) {
    g => select(g) orderBy(g.id desc)
  }

//join
  def allMatchHistory = 
    join(allQ, playersTable, playersTable)((g, a, b) =>
    select(a.name, b.name, g.win, g.lose, g.newRatingReporter, g.newRatingOpponent, g.reportedDate)
    on(g.reporter === a.id, g.opponent === b.id)
  )
//insert
  def add(game: ReportedGame) = inTransaction{
    reportedGamesTable.insert(game)
  }
}

//enumerator of game status
object GameStatus extends Enumeration {
  type GameStatus = Value
  val Reported = Value(1,"Reported")
  val Confirmed = Value(2,"Confirmed")
  val Rejected = Value(3,"Rejected")
}

//not model class. not correspond to a database table
case class Report(
  you: String, 
  opponent: String, 
  win: Int, 
  lose: Int, 
  password: String
)





