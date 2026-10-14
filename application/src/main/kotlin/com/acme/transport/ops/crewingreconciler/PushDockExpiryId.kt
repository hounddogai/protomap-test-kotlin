package com.acme.transport.ops.crewingreconciler

import com.acme.returns.v1.ReturnRequestCalculationServiceGrpcKt
import com.acme.returns.v1.calculateReturnRequestRequest
import com.acme.returns.v1.returnRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.DockLifecycleServiceGrpcKt
import com.acme.yards.v1.scheduleDockExpiryRequest
import io.grpc.ManagedChannel

/** Reads acme.yards.v1.DockLifecycleService/ScheduleDockExpiry and hands what it keeps to acme.returns.v1.ReturnRequestCalculationService. */
suspend fun pushDockExpiryId(channel: ManagedChannel) {
    val origin = DockLifecycleServiceGrpcKt.DockLifecycleServiceCoroutineStub(channel)
    val got = RpcBoundary.read { origin.scheduleDockExpiry(scheduleDockExpiryRequest { this.reason = "live-push-dock-expiry-id" }) }
    val rec = PushDockExpiryIdRecord(
        dockId = got.dock.dockId,
        name = normalize(got.dock.name),
    )
    PushDockExpiryIdReturnRequestCalculation(ReturnRequestCalculationServiceGrpcKt.ReturnRequestCalculationServiceCoroutineStub(channel)).write(rec)
}

/** What pushDockExpiryId keeps of each ScheduleDockExpiry response. */
data class PushDockExpiryIdRecord(val dockId: String, val name: String)

/** Passes records to acme.returns.v1.ReturnRequestCalculationService. */
class PushDockExpiryIdReturnRequestCalculation(private val stub: ReturnRequestCalculationServiceGrpcKt.ReturnRequestCalculationServiceCoroutineStub) {
    suspend fun write(rec: PushDockExpiryIdRecord) {
        val request = calculateReturnRequestRequest {
            this.returnRequest = returnRequest { this.number = rec.dockId; this.returnRequestId = rec.name }
        }
        stub.calculateReturnRequest(request)
    }
}
