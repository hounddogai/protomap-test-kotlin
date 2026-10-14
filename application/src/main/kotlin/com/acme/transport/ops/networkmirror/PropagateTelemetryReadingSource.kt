package com.acme.transport.ops.networkmirror

import com.acme.deliveries.v1.DeliveryExceptionApprovalServiceGrpcKt
import com.acme.deliveries.v1.escalateDeliveryExceptionRequest
import com.acme.dispatch.v1.DispatchAssignmentValidationServiceGrpcKt
import com.acme.dispatch.v1.getDispatchAssignmentRulesRequest
import com.acme.fleet.v1.DelegateVehicleGroupApprovalRequestKt
import com.acme.fleet.v1.VehicleGroupApprovalServiceGrpcKt
import com.acme.fleet.v1.delegateVehicleGroupApprovalRequest
import com.acme.fleet.v1.vehicleGroup
import com.acme.telematics.v1.TelemetryReadingValidationServiceGrpcKt
import com.acme.telematics.v1.validateTelemetryReadingRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.telematics.v1.TelemetryReadingValidationService/ValidateTelemetryReading and hands what it keeps to the sink of its kind. */
suspend fun propagateTelemetryReadingSource(channel: ManagedChannel) {
    val upstream = TelemetryReadingValidationServiceGrpcKt.TelemetryReadingValidationServiceCoroutineStub(channel)
    val current = RpcBoundary.read { upstream.validateTelemetryReading(validateTelemetryReadingRequest { this.reason = "current-propagate-telemetry-reading-source" }) }
    val record = PropagateTelemetryReadingSourceRecord(
        source = current.telemetryReading.source,
        message = current.result.message,
        correlationId = current.telemetryReading.correlationId,
    )
    dispatchPropagateTelemetryReadingSource("vehicle-group-approval", record, channel)
}

/** Where propagateTelemetryReadingSource hands each record. */
interface PropagateTelemetryReadingSourceTarget {
    suspend fun hand(rec: PropagateTelemetryReadingSourceRecord)
}

/** What propagateTelemetryReadingSource keeps of each ValidateTelemetryReading response. */
data class PropagateTelemetryReadingSourceRecord(val source: String, val message: String, val correlationId: String)

/** Sends records to acme.dispatch.v1.DispatchAssignmentValidationService. */
class PropagateTelemetryReadingSourceDispatchAssignmentValidation(private val stub: DispatchAssignmentValidationServiceGrpcKt.DispatchAssignmentValidationServiceCoroutineStub) : PropagateTelemetryReadingSourceTarget {
    override suspend fun hand(rec: PropagateTelemetryReadingSourceRecord) {
        stub.getDispatchAssignmentRules(getDispatchAssignmentRulesRequest { this.dispatchAssignmentId = rec.message })
    }
}

/** Passes records to acme.fleet.v1.VehicleGroupApprovalService. */
class PropagateTelemetryReadingSourceVehicleGroupApproval(private val stub: VehicleGroupApprovalServiceGrpcKt.VehicleGroupApprovalServiceCoroutineStub) : PropagateTelemetryReadingSourceTarget {
    override suspend fun hand(rec: PropagateTelemetryReadingSourceRecord) {
        val request = delegateVehicleGroupApprovalRequest {
            this.details = DelegateVehicleGroupApprovalRequestKt.details { this.approverEmail = rec.source }
            this.vehicleGroup = vehicleGroup { this.vehicleGroupId = rec.message }
        }
        stub.delegateVehicleGroupApproval(request)
    }
}

/** Sends records to acme.deliveries.v1.DeliveryExceptionApprovalService. */
class PropagateTelemetryReadingSourceDeliveryExceptionApproval(private val stub: DeliveryExceptionApprovalServiceGrpcKt.DeliveryExceptionApprovalServiceCoroutineStub) : PropagateTelemetryReadingSourceTarget {
    override suspend fun hand(rec: PropagateTelemetryReadingSourceRecord) {
        val request = escalateDeliveryExceptionRequest {
            this.deliveryExceptionId = rec.source
            this.reason = rec.message
        }
        stub.escalateDeliveryException(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class PropagateTelemetryReadingSourceLogOnly : PropagateTelemetryReadingSourceTarget {
    override suspend fun hand(rec: PropagateTelemetryReadingSourceRecord) {
        LoggerFactory.getLogger("PropagateTelemetryReadingSourceRecord").atInfo().addKeyValue("source", rec.source).log("No sink for record")
    }
}

/** Picks the writer of a kind and runs it. */
suspend fun dispatchPropagateTelemetryReadingSource(kind: String, rec: PropagateTelemetryReadingSourceRecord, channel: ManagedChannel) {
    var write: suspend (PropagateTelemetryReadingSourceRecord) -> Unit = PropagateTelemetryReadingSourceLogOnly()::hand
    when (kind) {
        "dispatch-assignment-validation" -> write = PropagateTelemetryReadingSourceDispatchAssignmentValidation(DispatchAssignmentValidationServiceGrpcKt.DispatchAssignmentValidationServiceCoroutineStub(channel))::hand
        "vehicle-group-approval" -> write = PropagateTelemetryReadingSourceVehicleGroupApproval(VehicleGroupApprovalServiceGrpcKt.VehicleGroupApprovalServiceCoroutineStub(channel))::hand
        "delivery-exception-approval" -> write = PropagateTelemetryReadingSourceDeliveryExceptionApproval(DeliveryExceptionApprovalServiceGrpcKt.DeliveryExceptionApprovalServiceCoroutineStub(channel))::hand
    }
    write(rec)
}
