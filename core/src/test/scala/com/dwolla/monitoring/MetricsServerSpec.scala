package com.dwolla.monitoring

import cats.effect._
import com.comcast.ip4s._
import com.eed3si9n.expecty.Expecty.expect
import io.chrisdavenport.epimetheus.CollectorRegistry
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.effect.PropF

import scala.concurrent.duration.DurationInt

class MetricsServerSpec extends CatsEffectSuite with ScalaCheckEffectSuite {
  test("Metrics server will shut down cleanly (with finalizers) when invoked as a Resource") {
    PropF.forAllF { (hasPort: Boolean) =>
      for {
        started <- Deferred[IO, Unit]
        closed <- Ref[IO].of(false)
        rsrc = Resource.make(started.complete(()).void)(_ => closed.set(true))
        registry <- CollectorRegistry.build[IO]
        fiber <- MetricsServer(registry, Option(port"0").filter(_ => hasPort)).runWithMetrics(rsrc).useForever.start
        _ <- started.get.timeout(100.millis) // wait to make sure things have started before we cancel them
        _ <- fiber.cancel
        isClosed <- closed.get
      } yield expect(isClosed)
    }
  }
}
