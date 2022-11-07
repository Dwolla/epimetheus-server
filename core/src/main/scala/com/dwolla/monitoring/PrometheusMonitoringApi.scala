package com.dwolla.monitoring

import cats._
import cats.implicits._
import com.dwolla.monitoring.PrometheusMonitoringApi.indexResponse
import io.chrisdavenport.epimetheus.CollectorRegistry
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Content-Type`
import org.http4s.implicits._

class PrometheusMonitoringApi[F[_] : Monad](collectorRegistry: CollectorRegistry[F])
                                           (implicit EE: EntityEncoder[F, String]) extends Http4sDsl[F] {
  def routesWithDefaults: HttpRoutes[F] =
    routes <+> HttpRoutes.of[F] {
      case GET -> Root => Ok(indexResponse)
      case GET -> Root / "health" => NoContent()
    }

  def routes: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / "metrics" =>
        collectorRegistry
          .write004
          .map {
            Response[F](Ok)
              .withEntity(_)
              .withContentType(`Content-Type`(mediaType"text/plain; version=0.0.4"))
          }
    }
}

object PrometheusMonitoringApi {
  private[PrometheusMonitoringApi] val indexResponse =
    """<html>
      |  <head><title>Admin</title></head>
      |  <body>
      |    <h1>Admin</h1>
      |    <ul>
      |      <li><a href="/health">Health Check</a></li>
      |      <li><a href="/metrics">Prometheus Metrics</a></li>
      |    </ul>
      |  </body>
      |</html>
      |""".stripMargin
}

@deprecated("use PrometheusMonitoringApi for better type inference", "v7.1.0")
class MonitoringApi[F[_] : Monad](implicit EE: EntityEncoder[F, String]) extends Http4sDsl[F] {
  def routesWithDefaults(collectorRegistry: CollectorRegistry[F]): HttpRoutes[F] =
    new PrometheusMonitoringApi(collectorRegistry).routesWithDefaults

  def routes(collectorRegistry: CollectorRegistry[F]): HttpRoutes[F] =
    new PrometheusMonitoringApi(collectorRegistry).routes
}

object MonitoringApi {}
