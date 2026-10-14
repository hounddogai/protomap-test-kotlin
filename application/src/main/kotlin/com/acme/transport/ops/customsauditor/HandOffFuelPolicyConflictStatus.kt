package com.acme.transport.ops.customsauditor

import com.acme.fuel.v1.FuelPolicySyncServiceGrpcKt
import com.acme.fuel.v1.resolveFuelPolicyConflictRequest
import com.acme.telematics.v1.TripLogCalculationServiceGrpcKt
import com.acme.telematics.v1.recalculateTripLogsRequest
import com.acme.telematics.v1.tripLog
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.DockCalculationServiceGrpcKt
import com.acme.yards.v1.dock
import com.acme.yards.v1.quoteDockRequest
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.fuel.v1.FuelPolicySyncService/ResolveFuelPolicyConflict and hands what it keeps to the sink of its kind. */
suspend fun handOffFuelPolicyConflictStatus(channel: ManagedChannel) {
    val origin = FuelPolicySyncServiceGrpcKt.FuelPolicySyncServiceCoroutineStub(channel)
    val got = RpcBoundary.read { origin.resolveFuelPolicyConflict(resolveFuelPolicyConflictRequest { this.fuelPolicyId = "default-hand-off-fuel-policy-conflict-status" }) }
    val record = HandOffFuelPolicyConflictStatusRecord(
        status = got.outcome.status,
        message = normalize(got.result.message),
    )
    dispatchHandOffFuelPolicyConflictStatus("dock-calculation", record, channel)
}

/** Where handOffFuelPolicyConflictStatus hands each record. */
interface HandOffFuelPolicyConflictStatusTarget {
    suspend fun hand(rec: HandOffFuelPolicyConflictStatusRecord)
}

/** What handOffFuelPolicyConflictStatus keeps of each ResolveFuelPolicyConflict response. */
data class HandOffFuelPolicyConflictStatusRecord(val status: String, val message: String)

/** Passes records to acme.telematics.v1.TripLogCalculationService. */
class HandOffFuelPolicyConflictStatusTripLogCalculation(private val stub: TripLogCalculationServiceGrpcKt.TripLogCalculationServiceCoroutineStub) : HandOffFuelPolicyConflictStatusTarget {
    override suspend fun hand(rec: HandOffFuelPolicyConflictStatusRecord) {
        val request = recalculateTripLogsRequest {
            this.tripLogId = rec.message
            this.tripLog = tripLog { this.tripLogId = rec.status }
        }
        RpcBoundary.run("acme.telematics.v1.TripLogCalculationService/RecalculateTripLogs") { stub.recalculateTripLogs(request) }
    }
}

/** Passes records to acme.yards.v1.DockCalculationService. */
class HandOffFuelPolicyConflictStatusDockCalculation(private val stub: DockCalculationServiceGrpcKt.DockCalculationServiceCoroutineStub) : HandOffFuelPolicyConflictStatusTarget {
    override suspend fun hand(rec: HandOffFuelPolicyConflictStatusRecord) {
        stub.quoteDock(quoteDockRequest { this.dock = dock { this.dockId = rec.message } })
    }
}

/** Only logs, for kinds no service takes yet. */
class HandOffFuelPolicyConflictStatusLogOnly : HandOffFuelPolicyConflictStatusTarget {
    override suspend fun hand(rec: HandOffFuelPolicyConflictStatusRecord) {
        LoggerFactory.getLogger("HandOffFuelPolicyConflictStatusRecord").atInfo().addKeyValue("status", rec.status).log("No sink for record")
    }
}

/** Picks the writer of a kind and runs it. */
suspend fun dispatchHandOffFuelPolicyConflictStatus(kind: String, rec: HandOffFuelPolicyConflictStatusRecord, channel: ManagedChannel) {
    var write: suspend (HandOffFuelPolicyConflictStatusRecord) -> Unit = HandOffFuelPolicyConflictStatusLogOnly()::hand
    when (kind) {
        "trip-log-calculation" -> write = HandOffFuelPolicyConflictStatusTripLogCalculation(TripLogCalculationServiceGrpcKt.TripLogCalculationServiceCoroutineStub(channel))::hand
        "dock-calculation" -> write = HandOffFuelPolicyConflictStatusDockCalculation(DockCalculationServiceGrpcKt.DockCalculationServiceCoroutineStub(channel))::hand
    }
    write(rec)
}
