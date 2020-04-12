package com.srs.demowebapi

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._

object Main extends IOApp {
  def run(args: List[String]) =
    DemowebapiServer.stream[IO].compile.drain.as(ExitCode.Success)
}