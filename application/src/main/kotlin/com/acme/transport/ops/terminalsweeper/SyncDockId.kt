package com.acme.transport.ops.terminalsweeper

import com.acme.customers.v1.PreferenceValidationServiceGrpcKt
import com.acme.customers.v1.preference
import com.acme.customers.v1.validatePreferenceRequest
import com.acme.dispatch.v1.DispatchOrderValidationServiceGrpcKt
import com.acme.dispatch.v1.dispatchOrder
import com.acme.dispatch.v1.explainDispatchOrderDecisionRequest
import com.acme.driverapp.v1.DriverHomeGatewayServiceGrpcKt
import com.acme.driverapp.v1.composeDriverHomePageRequest
import com.acme.driverapp.v1.driverHome
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.DockLifecycleServiceGrpcKt
import com.acme.yards.v1.YardValidationServiceGrpcKt
import com.acme.yards.v1.getYardRulesRequest
import com.acme.yards.v1.renewDockRequest
import io.grpc.ManagedChannel

/** Reads acme.yards.v1.DockLifecycleService/RenewDock and hands what it keeps to the sink of its kind. */
suspend fun syncDockId(channel: ManagedChannel) {
    val source = DockLifecycleServiceGrpcKt.DockLifecycleServiceCoroutineStub(channel)
    val reply = source.renewDock(renewDockRequest { this.dockId = "nightly-sync-dock-id" })
    val row = SyncDockIdRecord(
        dockId = reply.dock.dockId,
        name = reply.dock.name,
        message = reply.outcome.message,
        status = reply.result.status,
    )
    val registry = mutableMapOf<String, SyncDockIdDestination>()
    for (registration in listOf(
        SyncDockIdRegistration("dispatch-order-validation", SyncDockIdDispatchOrderValidation(DispatchOrderValidationServiceGrpcKt.DispatchOrderValidationServiceCoroutineStub(channel))),
        SyncDockIdRegistration("preference-validation", SyncDockIdPreferenceValidation(PreferenceValidationServiceGrpcKt.PreferenceValidationServiceCoroutineStub(channel))),
        SyncDockIdRegistration("yard-validation", SyncDockIdYardValidation(YardValidationServiceGrpcKt.YardValidationServiceCoroutineStub(channel))),
        SyncDockIdRegistration("driver-home-gateway", SyncDockIdDriverHomeGateway(DriverHomeGatewayServiceGrpcKt.DriverHomeGatewayServiceCoroutineStub(channel))),
    )) {
        registry[registration.kind] = registration.sink
    }
    val sink = registry["yard-validation"] ?: return
    sink.relay(row)
}

/** Where syncDockId hands each record. */
interface SyncDockIdDestination {
    suspend fun relay(rec: SyncDockIdRecord)
}

/** What syncDockId keeps of each RenewDock response. */
data class SyncDockIdRecord(val dockId: String, val name: String, val message: String, val status: String)

/** Passes records to acme.dispatch.v1.DispatchOrderValidationService. */
class SyncDockIdDispatchOrderValidation(private val stub: DispatchOrderValidationServiceGrpcKt.DispatchOrderValidationServiceCoroutineStub) : SyncDockIdDestination {
    override suspend fun relay(rec: SyncDockIdRecord) {
        val request = explainDispatchOrderDecisionRequest {
            this.dispatchOrder = dispatchOrder { this.notes = rec.name; this.number = rec.message; this.dispatchOrderId = rec.status }
        }
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderValidationService/ExplainDispatchOrderDecision") { stub.explainDispatchOrderDecision(request) }
    }
}

/** Passes records to acme.customers.v1.PreferenceValidationService. */
class SyncDockIdPreferenceValidation(private val stub: PreferenceValidationServiceGrpcKt.PreferenceValidationServiceCoroutineStub) : SyncDockIdDestination {
    override suspend fun relay(rec: SyncDockIdRecord) {
        val request = validatePreferenceRequest {
            this.reason = rec.status
            this.preference = preference { this.name = rec.message }
            this.preferenceId = rec.dockId
        }
        stub.validatePreference(request)
    }
}

/** Hands records to acme.yards.v1.YardValidationService. */
class SyncDockIdYardValidation(private val stub: YardValidationServiceGrpcKt.YardValidationServiceCoroutineStub) : SyncDockIdDestination {
    override suspend fun relay(rec: SyncDockIdRecord) {
        stub.getYardRules(getYardRulesRequest { this.yardId = rec.status })
    }
}

/** Hands records to acme.driverapp.v1.DriverHomeGatewayService. */
class SyncDockIdDriverHomeGateway(private val stub: DriverHomeGatewayServiceGrpcKt.DriverHomeGatewayServiceCoroutineStub) : SyncDockIdDestination {
    override suspend fun relay(rec: SyncDockIdRecord) {
        val request = composeDriverHomePageRequest {
            this.driverHome = driverHome { this.driverHomeId = rec.dockId; this.subject = rec.name }
        }
        stub.composeDriverHomePage(request)
    }
}

/** A sink and the kind of record it takes. */
class SyncDockIdRegistration(val kind: String, val sink: SyncDockIdDestination)
