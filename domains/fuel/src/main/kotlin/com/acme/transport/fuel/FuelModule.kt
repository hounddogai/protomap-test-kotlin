package com.acme.transport.fuel

import com.acme.transport.fuel.adapters.FuelCardExportController
import com.acme.transport.fuel.adapters.FuelCardQueryController
import com.acme.transport.fuel.adapters.FuelCardSyncController
import com.acme.transport.fuel.adapters.FuelPolicyApprovalController
import com.acme.transport.fuel.adapters.FuelPolicyHistoryController
import com.acme.transport.fuel.adapters.FuelPolicyReportingController
import com.acme.transport.fuel.adapters.FuelPolicyWorkflowController
import com.acme.transport.fuel.adapters.FuelTransactionExportController
import com.acme.transport.fuel.adapters.FuelTransactionQueryController
import com.acme.transport.fuel.adapters.FuelTransactionValidationController
import com.acme.transport.fuel.api.FuelCardCalculationGrpcService
import com.acme.transport.fuel.api.FuelCardGrpcService
import com.acme.transport.fuel.api.FuelCardLifecycleGrpcService
import com.acme.transport.fuel.api.FuelCardWorkflowGrpcService
import com.acme.transport.fuel.api.FuelPolicyExportGrpcService
import com.acme.transport.fuel.api.FuelPolicyLifecycleGrpcService
import com.acme.transport.fuel.api.FuelPolicyValidationGrpcService
import com.acme.transport.fuel.api.FuelTransactionCalculationGrpcService
import com.acme.transport.fuel.api.FuelTransactionLifecycleGrpcService
import com.acme.transport.fuel.api.FuelTransactionSyncGrpcService
import com.acme.transport.fuel.grpc.FuelCardApprovalEndpoint
import com.acme.transport.fuel.grpc.FuelCardImportEndpoint
import com.acme.transport.fuel.grpc.FuelCardReportingEndpoint
import com.acme.transport.fuel.grpc.FuelCardValidationEndpoint
import com.acme.transport.fuel.grpc.FuelPolicyCalculationEndpoint
import com.acme.transport.fuel.grpc.FuelPolicyImportEndpoint
import com.acme.transport.fuel.grpc.FuelPolicySyncEndpoint
import com.acme.transport.fuel.grpc.FuelTransactionApprovalEndpoint
import com.acme.transport.fuel.grpc.FuelTransactionImportEndpoint
import com.acme.transport.fuel.grpc.FuelTransactionReportingEndpoint
import com.acme.transport.fuel.grpc.FuelTransactionWorkflowEndpoint
import io.grpc.BindableService
import org.koin.core.Koin
import org.koin.dsl.module

/** Wiring and service registration for the fuel team. */
val fuelModule = module {
    single { FuelRepository(get()) }
    single { FuelCardApprovalEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { FuelCardCalculationGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { FuelCardExportController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { FuelCardImportEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { FuelCardLifecycleGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { FuelCardQueryController(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { FuelCardReportingEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { FuelCardGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { FuelCardSyncController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { FuelCardValidationEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { FuelCardWorkflowGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { FuelPolicyApprovalController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { FuelPolicyCalculationEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { FuelPolicyExportGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { FuelPolicyHistoryController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { FuelPolicyImportEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { FuelPolicyLifecycleGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { FuelPolicyReportingController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { FuelPolicySyncEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { FuelPolicyValidationGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { FuelPolicyWorkflowController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { FuelTransactionApprovalEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { FuelTransactionCalculationGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { FuelTransactionExportController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { FuelTransactionImportEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { FuelTransactionLifecycleGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { FuelTransactionQueryController(get(), get(), get(), get(), get(), get(), get()) }
    single { FuelTransactionReportingEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { FuelTransactionSyncGrpcService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { FuelTransactionValidationController(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { FuelTransactionWorkflowEndpoint(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
}

fun fuelServices(koin: Koin): List<BindableService> = listOf(
    koin.get<FuelCardApprovalEndpoint>(),
    koin.get<FuelCardCalculationGrpcService>(),
    koin.get<FuelCardExportController>(),
    koin.get<FuelCardImportEndpoint>(),
    koin.get<FuelCardLifecycleGrpcService>(),
    koin.get<FuelCardQueryController>(),
    koin.get<FuelCardReportingEndpoint>(),
    koin.get<FuelCardGrpcService>(),
    koin.get<FuelCardSyncController>(),
    koin.get<FuelCardValidationEndpoint>(),
    koin.get<FuelCardWorkflowGrpcService>(),
    koin.get<FuelPolicyApprovalController>(),
    koin.get<FuelPolicyCalculationEndpoint>(),
    koin.get<FuelPolicyExportGrpcService>(),
    koin.get<FuelPolicyHistoryController>(),
    koin.get<FuelPolicyImportEndpoint>(),
    koin.get<FuelPolicyLifecycleGrpcService>(),
    koin.get<FuelPolicyReportingController>(),
    koin.get<FuelPolicySyncEndpoint>(),
    koin.get<FuelPolicyValidationGrpcService>(),
    koin.get<FuelPolicyWorkflowController>(),
    koin.get<FuelTransactionApprovalEndpoint>(),
    koin.get<FuelTransactionCalculationGrpcService>(),
    koin.get<FuelTransactionExportController>(),
    koin.get<FuelTransactionImportEndpoint>(),
    koin.get<FuelTransactionLifecycleGrpcService>(),
    koin.get<FuelTransactionQueryController>(),
    koin.get<FuelTransactionReportingEndpoint>(),
    koin.get<FuelTransactionSyncGrpcService>(),
    koin.get<FuelTransactionValidationController>(),
    koin.get<FuelTransactionWorkflowEndpoint>(),
)
