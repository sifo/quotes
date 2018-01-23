import java.nio.file.Paths

import akka.http.scaladsl._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream._
import akka.stream.ActorMaterializer
import akka.stream.ClosedShape
import akka.stream.scaladsl._
import akka.util.ByteString

import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.concurrent._
import scala.util.Failure
import scala.util.Success
import scala.io.{ StdIn }

object WebServer {
  def main(args: Array[String]) {

    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val route =
      get {
        path("quotes.txt") {
          val fileio = FileIO.fromPath(Paths.get("src/main/resources/quotes.txt"))
          complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, fileio))

        } ~
        path("quotes") {
          parameters('author.*, 'quote.*, 'text.?) { (authors, quotes, text) =>
            val response: Source[ByteString, Any] = {
              val regex = """(.*)\t(.*)""".r
              val quotesSource = scala.io.Source.fromResource("quotes.txt")
                .getLines
                .map { l =>
                  l match {
                    case regex(a, q) => (a, q)
                  }
                }
                .filter { l =>
                  authors.toList match {
                    case Nil => true
                    case multiple =>
                      multiple.filter(a => l._1.toLowerCase.contains(a.toLowerCase)).size > 0
                  }
                }
                .filter { l =>
                  quotes.toList match {
                    case Nil => true
                    case multiple =>
                      multiple.filter(q => l._2.toLowerCase.contains(q.toLowerCase)).size > 0
                  }
                }
                .filter { l =>
                  text match {
                    case None => true
                    case Some(t) => t.toLowerCase.contains(l._2.toLowerCase)
                  }
                }
                .map { l =>
                  var newAuthor = l._1
                  var newQuote = l._2
                  authors.toList match {
                    case multiple =>
                      multiple.foreach { q =>
                        newAuthor = newAuthor.replaceAll(s"((?i)$q)", "<span style='background-color: #AED6F1'>$1</span>")
                      }
                  }
                  quotes.toList match {
                    case multiple =>
                      multiple.foreach { q =>
                        newQuote = newQuote.replaceAll(s"((?i)$q)", "<span style='background-color: #EDBB99'>$1</span>")
                      }
                  }
                  (newAuthor, newQuote)
                }
                .map(l => s"<p><strong>${l._1}:</strong> <q>${l._2}</q></p>\n")
                .map(l => ByteString(l))
              Source.fromIterator(() => quotesSource)
            }

            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, response))
          }
        }
      }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}
