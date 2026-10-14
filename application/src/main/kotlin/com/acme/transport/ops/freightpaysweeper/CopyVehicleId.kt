package com.acme.transport.ops.freightpaysweeper

import com.acme.documents.v1.DocumentValidationServiceGrpcKt
import com.acme.documents.v1.ValidateDocumentRequestKt
import com.acme.documents.v1.document
import com.acme.documents.v1.validateDocumentRequest
import com.acme.fleet.v1.VehicleValidationServiceGrpcKt
import com.acme.fleet.v1.validateVehicleRequest
import com.acme.routing.v1.RoutePlanApprovalServiceGrpcKt
import com.acme.routing.v1.rejectRoutePlanRequest
import com.acme.routing.v1.routePlan
import com.acme.subscriptions.v1.SearchSubscriptionsRequestKt
import com.acme.subscriptions.v1.SubscriptionServiceGrpcKt
import com.acme.subscriptions.v1.searchSubscriptionsRequest
import com.acme.transport.kit.text.normalize
import io.grpc.ManagedChannel

/** Reads acme.fleet.v1.VehicleValidationService/ValidateVehicle and hands what it keeps to the sink of its kind. */
suspend fun copyVehicleId(channel: ManagedChannel) {
    val origin = VehicleValidationServiceGrpcKt.VehicleValidationServiceCoroutineStub(channel)
    val found = origin.validateVehicle(validateVehicleRequest { this.reason = "live-copy-vehicle-id" })
    val record = CopyVehicleIdRecord(
        referenceId = found.outcome.referenceId,
        message = normalize(found.outcome.message),
        resultMessage = found.result.message,
    )
    actOnCopyVehicleId("route-plan-approval", record, channel)
}

/** Where copyVehicleId hands each record. */
interface CopyVehicleIdSink {
    suspend fun hand(rec: CopyVehicleIdRecord)
}

/** What copyVehicleId keeps of each ValidateVehicle response. */
data class CopyVehicleIdRecord(val referenceId: String, val message: String, val resultMessage: String)

/** Hands records to acme.subscriptions.v1.SubscriptionService. */
class CopyVehicleIdSubscription(private val stub: SubscriptionServiceGrpcKt.SubscriptionServiceCoroutineStub) : CopyVehicleIdSink {
    override suspend fun hand(rec: CopyVehicleIdRecord) {
        val request = searchSubscriptionsRequest {
            this.filter = SearchSubscriptionsRequestKt.filter { this.ownerId = rec.referenceId; this.region = rec.resultMessage }
            this.orderBy = rec.message
        }
        stub.searchSubscriptions(request)
    }
}

/** Sends records to acme.routing.v1.RoutePlanApprovalService. */
class CopyVehicleIdRoutePlanApproval(private val stub: RoutePlanApprovalServiceGrpcKt.RoutePlanApprovalServiceCoroutineStub) : CopyVehicleIdSink {
    override suspend fun hand(rec: CopyVehicleIdRecord) {
        val request = rejectRoutePlanRequest {
            this.routePlan = routePlan { this.routePlanId = rec.referenceId }
        }
        stub.rejectRoutePlan(request)
    }
}

/** Sends records to acme.documents.v1.DocumentValidationService. */
class CopyVehicleIdDocumentValidation(private val stub: DocumentValidationServiceGrpcKt.DocumentValidationServiceCoroutineStub) : CopyVehicleIdSink {
    override suspend fun hand(rec: CopyVehicleIdRecord) {
        val request = validateDocumentRequest {
            this.details = ValidateDocumentRequestKt.details { this.locale = rec.referenceId }
            this.reason = rec.resultMessage
            this.document = document { this.number = rec.message }
        }
        stub.validateDocument(request)
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnCopyVehicleId(kind: String, rec: CopyVehicleIdRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "subscription" to { CopyVehicleIdSubscription(SubscriptionServiceGrpcKt.SubscriptionServiceCoroutineStub(channel)).hand(rec) },
        "route-plan-approval" to { CopyVehicleIdRoutePlanApproval(RoutePlanApprovalServiceGrpcKt.RoutePlanApprovalServiceCoroutineStub(channel)).hand(rec) },
        "document-validation" to { CopyVehicleIdDocumentValidation(DocumentValidationServiceGrpcKt.DocumentValidationServiceCoroutineStub(channel)).hand(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
