package com.acme.transport.ops.lastmilebackfill

import com.acme.customers.v1.HouseholdQueryServiceGrpcKt
import com.acme.customers.v1.browseHouseholdsRequest
import com.acme.fleet.v1.VehicleGroupValidationServiceGrpcKt
import com.acme.fleet.v1.checkVehicleGroupEligibilityRequest
import com.acme.fleet.v1.vehicleGroup
import com.acme.suppliers.v1.SupplierServiceGrpcKt
import com.acme.suppliers.v1.getSupplierRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.YardValidationServiceGrpcKt
import com.acme.yards.v1.checkYardEligibilityRequest
import com.acme.yards.v1.yard
import io.grpc.ManagedChannel

/** Reads acme.customers.v1.HouseholdQueryService/BrowseHouseholds and hands what it keeps to the sink of its kind. */
suspend fun handOffHouseholdsStatus(channel: ManagedChannel) {
    val origin = HouseholdQueryServiceGrpcKt.HouseholdQueryServiceCoroutineStub(channel)
    val current = origin.browseHouseholds(browseHouseholdsRequest { this.orderBy = "default-hand-off-households-status" })
    val record = HandOffHouseholdsStatusRecord(
        status = normalize(current.result.status),
        message = current.result.message,
    )
    val sinks = mapOf<String, HandOffHouseholdsStatusDestination>(
        "yard-validation" to HandOffHouseholdsStatusYardValidation(YardValidationServiceGrpcKt.YardValidationServiceCoroutineStub(channel)),
        "vehicle-group-validation" to HandOffHouseholdsStatusVehicleGroupValidation(VehicleGroupValidationServiceGrpcKt.VehicleGroupValidationServiceCoroutineStub(channel)),
        "supplier" to HandOffHouseholdsStatusSupplier(SupplierServiceGrpcKt.SupplierServiceCoroutineStub(channel)),
    )
    routeHandOffHouseholdsStatus(sinks, "vehicle-group-validation", record)
}

/** Where handOffHouseholdsStatus hands each record. */
interface HandOffHouseholdsStatusDestination {
    suspend fun deliver(rec: HandOffHouseholdsStatusRecord)
}

/** What handOffHouseholdsStatus keeps of each BrowseHouseholds response. */
data class HandOffHouseholdsStatusRecord(val status: String, val message: String)

/** Sends records to acme.yards.v1.YardValidationService. */
class HandOffHouseholdsStatusYardValidation(private val stub: YardValidationServiceGrpcKt.YardValidationServiceCoroutineStub) : HandOffHouseholdsStatusDestination {
    override suspend fun deliver(rec: HandOffHouseholdsStatusRecord) {
        val request = checkYardEligibilityRequest {
            this.yard = yard { this.code = rec.message; this.name = rec.status }
        }
        RpcBoundary.run("acme.yards.v1.YardValidationService/CheckYardEligibility") { stub.checkYardEligibility(request) }
    }
}

/** Hands records to acme.fleet.v1.VehicleGroupValidationService. */
class HandOffHouseholdsStatusVehicleGroupValidation(private val stub: VehicleGroupValidationServiceGrpcKt.VehicleGroupValidationServiceCoroutineStub) : HandOffHouseholdsStatusDestination {
    override suspend fun deliver(rec: HandOffHouseholdsStatusRecord) {
        val request = checkVehicleGroupEligibilityRequest {
            this.reason = rec.message
            this.vehicleGroup = vehicleGroup { this.parentId = rec.status }
        }
        stub.checkVehicleGroupEligibility(request)
    }
}

/** Writes records to acme.suppliers.v1.SupplierService. */
class HandOffHouseholdsStatusSupplier(private val stub: SupplierServiceGrpcKt.SupplierServiceCoroutineStub) : HandOffHouseholdsStatusDestination {
    override suspend fun deliver(rec: HandOffHouseholdsStatusRecord) {
        stub.getSupplier(getSupplierRequest { this.supplierId = rec.message })
    }
}

/** Hands a record to the sink registered for its kind. */
suspend fun routeHandOffHouseholdsStatus(sinks: Map<String, HandOffHouseholdsStatusDestination>, kind: String, rec: HandOffHouseholdsStatusRecord) {
    val sink = sinks[kind] ?: error("No sink registered for $kind")
    sink.deliver(rec)
}
