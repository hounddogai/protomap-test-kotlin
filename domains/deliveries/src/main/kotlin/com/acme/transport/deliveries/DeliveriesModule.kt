package com.acme.transport.deliveries

import com.acme.transport.deliveries.adapters.DeliveryExceptionCalculationController
import com.acme.transport.deliveries.adapters.DeliveryQueryController
import com.acme.transport.deliveries.adapters.DeliveryWorkflowController
import com.acme.transport.deliveries.adapters.ProofOfDeliveryQueryController
import com.acme.transport.deliveries.api.DeliveryExceptionApprovalGrpcService
import com.acme.transport.deliveries.api.DeliveryExceptionWorkflowGrpcService
import com.acme.transport.deliveries.api.DeliveryValidationGrpcService
import com.acme.transport.deliveries.api.ProofOfDeliveryCalculationGrpcService
import com.acme.transport.deliveries.api.ProofOfDeliveryWorkflowGrpcService
import com.acme.transport.deliveries.grpc.DeliveryApprovalEndpoint
import com.acme.transport.deliveries.grpc.DeliveryEndpoint
import com.acme.transport.deliveries.grpc.DeliveryExceptionValidationEndpoint
import com.acme.transport.deliveries.grpc.ProofOfDeliveryApprovalEndpoint
import com.acme.transport.deliveries.grpc.ProofOfDeliveryValidationEndpoint
import io.grpc.BindableService
import org.koin.core.Koin
import org.koin.dsl.module

/** Wiring and service registration for the deliveries team. */
val deliveriesModule = module {
    single { DeliveriesRepository(get()) }
    single { DeliveryApprovalEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DeliveryExceptionApprovalGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DeliveryExceptionCalculationController(get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DeliveryExceptionValidationEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DeliveryExceptionWorkflowGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DeliveryQueryController(get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DeliveryEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { DeliveryValidationGrpcService(get(), get(), get(), get(), get(), get(), get()) }
    single { DeliveryWorkflowController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { ProofOfDeliveryApprovalEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { ProofOfDeliveryCalculationGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { ProofOfDeliveryQueryController(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { ProofOfDeliveryValidationEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { ProofOfDeliveryWorkflowGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
}

fun deliveriesServices(koin: Koin): List<BindableService> = listOf(
    koin.get<DeliveryApprovalEndpoint>(),
    koin.get<DeliveryExceptionApprovalGrpcService>(),
    koin.get<DeliveryExceptionCalculationController>(),
    koin.get<DeliveryExceptionValidationEndpoint>(),
    koin.get<DeliveryExceptionWorkflowGrpcService>(),
    koin.get<DeliveryQueryController>(),
    koin.get<DeliveryEndpoint>(),
    koin.get<DeliveryValidationGrpcService>(),
    koin.get<DeliveryWorkflowController>(),
    koin.get<ProofOfDeliveryApprovalEndpoint>(),
    koin.get<ProofOfDeliveryCalculationGrpcService>(),
    koin.get<ProofOfDeliveryQueryController>(),
    koin.get<ProofOfDeliveryValidationEndpoint>(),
    koin.get<ProofOfDeliveryWorkflowGrpcService>(),
)
