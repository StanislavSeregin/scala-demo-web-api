package com.srs.demowebapi

import cats.effect._
import cats.implicits._
import fs2.Stream
import org.http4s.HttpRoutes
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.syntax.kleisli._
import sttp.tapir._
import sttp.tapir.server.http4s._
import scala.concurrent.ExecutionContext

object Main extends IOApp {
  val _endpoint: Endpoint[String, Unit, String, Nothing] =
    endpoint.get.in("hello").in(query[String]("name")).out(stringBody)

  val routes: HttpRoutes[IO] =
    _endpoint.toRoutes(name => IO(s"Hello, $name!".asRight[Unit]))

  def run(args: List[String]): IO[ExitCode] =
    WebApiApp.serverStream[IO](routes).compile.drain.as(ExitCode.Success)
}

object WebApiApp {
  def serverStream[F[_]: ConcurrentEffect: Timer](routes: HttpRoutes[F]): Stream[F, ExitCode] =
    BlazeServerBuilder[F]
      .bindHttp(port = 8080, host = "localhost")
      .withHttpApp(Router("/" -> routes).orNotFound)
      .serve
}
