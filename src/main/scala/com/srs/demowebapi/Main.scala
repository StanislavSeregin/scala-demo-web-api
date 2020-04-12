package com.srs.demowebapi

import cats.effect._
import sttp.client._
import org.http4s.HttpRoutes
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.syntax.kleisli._
import sttp.tapir._
import sttp.tapir.server.http4s._
import cats.implicits._

import scala.concurrent.ExecutionContext

object Main extends App {
  val helloWorld: Endpoint[String, Unit, String, Nothing] =
    endpoint.get.in("hello").in(query[String]("name")).out(stringBody)

  implicit val ec: ExecutionContext           = scala.concurrent.ExecutionContext.Implicits.global
  implicit val contextShift: ContextShift[IO] = IO.contextShift(ec)
  implicit val timer: Timer[IO]               = IO.timer(ec)

  val helloWorldRoutes: HttpRoutes[IO] = helloWorld.toRoutes(name => IO(s"Hello, $name!".asRight[Unit]))

  BlazeServerBuilder[IO]
    .bindHttp(8080, "localhost")
    .withHttpApp(Router("/" -> helloWorldRoutes).orNotFound)
    .resource
    .use { _ =>
      IO {
        implicit val backend: SttpBackend[Identity, Nothing, NothingT] = HttpURLConnectionBackend()
        val result: String =
          basicRequest.response(asStringAlways).get(uri"http://localhost:8080/hello?name=Frodo").send().body
        println("Got result: " + result)

        assert(result == "Hello, Frodo!")
      }
    }
    .unsafeRunSync()
}
