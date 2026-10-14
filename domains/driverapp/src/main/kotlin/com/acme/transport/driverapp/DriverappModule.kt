package com.acme.transport.driverapp

import com.acme.transport.driverapp.adapters.DriverHomeIntegrationController
import com.acme.transport.driverapp.adapters.DriverHomeReportingController
import com.acme.transport.driverapp.adapters.DriverInboxApprovalController
import com.acme.transport.driverapp.adapters.DriverInboxQueryController
import com.acme.transport.driverapp.adapters.DriverInboxWorkflowController
import com.acme.transport.driverapp.api.DriverHomeGatewayGrpcService
import com.acme.transport.driverapp.api.DriverHomeQueryGrpcService
import com.acme.transport.driverapp.api.DriverHomeWorkflowGrpcService
import com.acme.transport.driverapp.api.DriverInboxGrpcService
import com.acme.transport.driverapp.api.DriverInboxNotificationGrpcService
import com.acme.transport.driverapp.grpc.DriverHomeApprovalEndpoint
import com.acme.transport.driverapp.grpc.DriverHomeEndpoint
import com.acme.transport.driverapp.grpc.DriverHomeNotificationEndpoint
import com.acme.transport.driverapp.grpc.DriverInboxIntegrationEndpoint
import com.acme.transport.driverapp.grpc.DriverInboxReportingEndpoint
import io.grpc.BindableService
import org.koin.core.Koin
import org.koin.dsl.module

/** Wiring and service registration for the driverapp team. */
val driverappModule = module {
    single { DriverappRepository(get()) }
    single { DriverHomeApprovalEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DriverHomeGatewayGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DriverHomeIntegrationController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DriverHomeNotificationEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DriverHomeQueryGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DriverHomeReportingController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DriverHomeEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DriverHomeWorkflowGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DriverInboxApprovalController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DriverInboxIntegrationEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DriverInboxNotificationGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DriverInboxQueryController(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DriverInboxReportingEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DriverInboxGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DriverInboxWorkflowController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
}

fun driverappServices(koin: Koin): List<BindableService> = listOf(
    koin.get<DriverHomeApprovalEndpoint>(),
    koin.get<DriverHomeGatewayGrpcService>(),
    koin.get<DriverHomeIntegrationController>(),
    koin.get<DriverHomeNotificationEndpoint>(),
    koin.get<DriverHomeQueryGrpcService>(),
    koin.get<DriverHomeReportingController>(),
    koin.get<DriverHomeEndpoint>(),
    koin.get<DriverHomeWorkflowGrpcService>(),
    koin.get<DriverInboxApprovalController>(),
    koin.get<DriverInboxIntegrationEndpoint>(),
    koin.get<DriverInboxNotificationGrpcService>(),
    koin.get<DriverInboxQueryController>(),
    koin.get<DriverInboxReportingEndpoint>(),
    koin.get<DriverInboxGrpcService>(),
    koin.get<DriverInboxWorkflowController>(),
)
