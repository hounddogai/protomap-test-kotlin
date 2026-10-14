package com.acme.transport.server

import com.acme.identity.v1.UserValidationServiceGrpcKt
import com.acme.transport.deliveries.deliveriesModule
import com.acme.transport.deliveries.deliveriesServices
import com.acme.transport.dispatch.dispatchModule
import com.acme.transport.dispatch.dispatchServices
import com.acme.transport.driverapp.driverappModule
import com.acme.transport.driverapp.driverappServices
import com.acme.transport.drivers.driversModule
import com.acme.transport.drivers.driversServices
import com.acme.transport.fleet.fleetModule
import com.acme.transport.fleet.fleetServices
import com.acme.transport.fuel.fuelModule
import com.acme.transport.fuel.fuelServices
import com.acme.transport.maintenance.maintenanceModule
import com.acme.transport.maintenance.maintenanceServices
import com.acme.transport.platform.CallerHeaders
import com.acme.transport.platform.IdentityInterceptor
import com.acme.transport.platform.JdbcOperationJournal
import com.acme.transport.platform.OperationJournal
import com.acme.transport.platform.Settings
import com.acme.transport.platform.clientsModule
import com.acme.transport.routing.routingModule
import com.acme.transport.routing.routingServices
import com.acme.transport.telematics.telematicsModule
import com.acme.transport.telematics.telematicsServices
import com.acme.transport.yards.yardsModule
import com.acme.transport.yards.yardsServices
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.ServerInterceptors
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.slf4j.LoggerFactory

/** One deployable registers the transport domains and owns their background workers. */
fun main() {
    val settings = Settings.fromEnvironment()
    val log = LoggerFactory.getLogger("acme.transport.server")
    val workers = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val executor = Executors.newVirtualThreadPerTaskExecutor()
    // Calls no caller asked for, such as a worker's, bear the server's own credential.
    val mesh = ManagedChannelBuilder.forTarget(settings.meshTarget).usePlaintext()
        .intercept(CallerHeaders("Bearer " + settings.serviceToken)).build()
    val identities = ManagedChannelBuilder.forTarget(settings.identityTarget).usePlaintext().build()
    val runtimeModule = module {
        single { settings }
        single { workers }
        single<ManagedChannel>(named("mesh")) { mesh }
        single<OperationJournal> { JdbcOperationJournal(settings.dataSource()) }
    }
    val application = startKoin {
        modules(clientsModule, runtimeModule, deliveriesModule, dispatchModule, driverappModule, driversModule, fleetModule, fuelModule, maintenanceModule, routingModule, telematicsModule, yardsModule)
    }
    val authentication = IdentityInterceptor(UserValidationServiceGrpcKt.UserValidationServiceCoroutineStub(identities), settings.serviceToken)
    val builder = NettyServerBuilder.forPort(settings.port).executor(executor)
    val services = deliveriesServices(application.koin) +
        dispatchServices(application.koin) +
        driverappServices(application.koin) +
        driversServices(application.koin) +
        fleetServices(application.koin) +
        fuelServices(application.koin) +
        maintenanceServices(application.koin) +
        routingServices(application.koin) +
        telematicsServices(application.koin) +
        yardsServices(application.koin)
    services.forEach { service -> builder.addService(ServerInterceptors.intercept(service, authentication)) }
    val server = builder.build().start()
    Runtime.getRuntime().addShutdownHook(Thread {
        server.shutdown().awaitTermination(10, TimeUnit.SECONDS)
        server.shutdownNow()
        workers.cancel()
        mesh.shutdownNow()
        identities.shutdownNow()
        executor.shutdown()
        application.close()
    })
    log.atInfo().addKeyValue("port", settings.port).addKeyValue("services", services.size).log("Transport server ready")
    server.awaitTermination()
}
