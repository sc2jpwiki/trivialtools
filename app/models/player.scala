package models

case class Player(
  name: String, raceId: Int, rating: Int)

object Player {

  var players = Set (
    Player("Albion", 0,530000),
    Player("PSiArc", 1,10000),
    Player("MoMoTaro",2,1200)
  )

  def findAll = players.toList.sortBy(-_.rating)

  def add(player: Player) {
    players = players + player
  }

}


