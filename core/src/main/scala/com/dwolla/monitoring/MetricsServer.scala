package com.dwolla.monitoring

import cats.effect.*
import cats.syntax.all.*
import com.comcast.ip4s.*
import fs2.Stream
import fs2.io.net.Network
import io.chrisdavenport.epimetheus.CollectorRegistry
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits.*

sealed abstract class MetricsServer[F[_] : Async : Network] private(registry: CollectorRegistry[F],
                                                                    port: Option[Port],
                                                                   ) {
  private val maybeServer: Option[Resource[F, Unit]] =
    port.map {
      EmberServerBuilder
        .default[F]
        .withHost(host"0.0.0.0")
        .withPort(_)
        .withHttpApp(new PrometheusMonitoringApi[F](registry).routesWithDefaults.orNotFound)
        .build
        .void
    }

  def runWithMetrics(program: Stream[F, Unit]): Stream[F, Unit] =
    maybeServer
      .map(_.useForever)
      .map(Stream.eval)
      // http server serves requests in the background while program runs, if port is defined
      .foldl(program)(_ concurrently _)

  def runWithMetrics(program: Resource[F, Unit]): Resource[F, Unit] =
    (maybeServer.foldA, program).parTupled.void

  def runWithMetrics(program: F[Unit]): F[Unit] =
    runWithMetrics(Stream.eval(program)).compile.lastOrError

}

object MetricsServer {
  @deprecated("Add Network constraint or use forAsync", "3.7.0")
  def apply[F[_]](registry: CollectorRegistry[F],
                          port: Option[Port],
                          F: Async[F]): MetricsServer[F] = {
    new MetricsServer(registry, port)(F, fs2.io.net.Network.implicitForAsync(F)) {}
  }

  def apply[F[_] : Async : Network](registry: CollectorRegistry[F],
                                    port: Option[Port],
                                   ): MetricsServer[F] = new MetricsServer(registry, port) {}
}
