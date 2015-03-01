package models

case class Player(
  name: String, raceId: Int, rating: Int, password: String)

object Player {

  var players = Set (
    Player("Albion", 0,530000,"prpr"),
    Player("PSiArc", 1,10000,"t1love"),
    Player("MoMoTaro",2,1200,"goji")
  )

  def findAll = players.toList.sortBy(-_.rating)

  def add(player: Player) {
    players = players + player
  }

}



