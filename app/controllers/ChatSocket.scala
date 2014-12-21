package controllers
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import play.api.libs.iteratee._
import play.api.libs.concurrent._
import play.api.mvc.WebSocket
import play.api.Play.current
import play.api.mvc.Controller
import play.api.mvc.Action
import play.api.libs.concurrent.Execution.Implicits._

object ChatController extends Controller {
    implicit val timeout = Timeout(1)
    val room = Akka.system.actorOf(Props[ChatRoom])

    def showRoom(nickName: String) = Action { implicit request =>
      Ok(views.html.chat(nickName))
    }
  
    def chatSocket(nickName: String) = WebSocket.async { request =>
      val channelsFuture = room ? Join(nickName)
          channelsFuture.mapTo[(Iteratee[String, _], Enumerator[String])]
  }
}

case class Join(nickName: String)
case class Leave(nickName: String)
case class Broadcast(msg: String)

class ChatRoom extends Actor {
    var users = Set[String]()
    val (enumerator, channel) = Concurrent.broadcast[String]
    def receive = {
        case Join(nickName) => {
            if (!users.contains(nickName)) {
                val iteratee = Iteratee.foreach[String]{ message =>
                  self ! Broadcast("%s: %s" format (nickName, message))
              }.mapDone{ _ =>
                self ! Leave(nickName)
            }
            users += nickName
            channel.push("User %s has joined the room, now %s users"
                format (nickName, users.size))
            sender ! (iteratee, enumerator)
        } else {
            val enumerator = Enumerator(
                "Nickname %s is already in use." format nickName)
            val iteratee = Iteratee.ignore
            sender ! (iteratee, enumerator)
        }
    }
    case Leave(nickName) => {
        users -= nickName
        channel.push("User %s has left the room, %s users left"
            format (nickName, users.size))
    }
    case Broadcast(msg: String) => channel.push(msg)
  }
}



