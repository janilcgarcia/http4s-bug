//> using scala "3.2.0"
//> using lib "org.typelevel::cats-effect:3.4.2"
//> using lib "co.fs2::fs2-core:3.4.0"
//> using lib "co.fs2::fs2-io:3.4.0"
//> using lib "org.http4s::http4s-dsl:1.0.0-M37"
//> using lib "org.http4s::http4s-ember-server:1.0.0-M37"
//> using lib "org.slf4j:slf4j-simple:2.0.5"

import cats.syntax.all.*
import cats.effect.{Async, ExitCode, IO, IOApp, Resource}
import cats.effect.syntax.all.*
import fs2.io.net.tls.TLSContext
import org.http4s.HttpRoutes
import org.http4s.MediaType
import org.http4s.headers.`Content-Type`
import org.http4s.dsl.Http4sDsl
import org.http4s.ember.server.EmberServerBuilder
import com.comcast.ip4s.port
import fs2.io.net.Network

import java.nio.file.{Path, Paths}

private class Server[F[_]: Async] private (tlsContext: TLSContext[F]) extends Http4sDsl[F]:
  private val app = HttpRoutes.of[F] {
    case GET -> Root / "bytes" / IntVar(numberOfBytes) =>
      val bytes = Array.fill(numberOfBytes)('A'.toByte)

      Ok(bytes).map(_.withContentType(`Content-Type`(MediaType.text.plain)))
  }.orNotFound

  private val baseServer = EmberServerBuilder
    .default[F]
    .withHttpApp(app)

  private val http2NoTls =
    baseServer
      .withHttp2
      .withPort(port"5000")
      .build

  private val http2Tls =
    baseServer
      .withHttp2
      .withTLS(tlsContext)
      .withPort(port"5001")
      .build

  private val http1NoTls =
    baseServer
      .withPort(port"5002")
      .build

  private val http1Tls =
    baseServer
      .withTLS(tlsContext)
      .withPort(port"5003")
      .build

  val servers: Resource[F, Seq[org.http4s.server.Server]] =
    List(http2NoTls, http2Tls, http1NoTls, http1Tls).sequence

object Server:
  def apply[F[_]: Async]: F[Server[F]] =
    val password = "123456".toCharArray
    val path = Paths.get("keystore.pkcs12")

    Network[F].tlsContext.fromKeyStoreFile(path, password, password)
      .map(new Server(_))


object RunServer extends IOApp:
  override def run(args: List[String]): IO[ExitCode] =
    Server[IO].flatMap(_.servers.useForever)
