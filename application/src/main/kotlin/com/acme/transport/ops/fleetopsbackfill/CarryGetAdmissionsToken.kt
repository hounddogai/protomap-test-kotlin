package com.acme.transport.ops.fleetopsbackfill

import com.acme.admissions.v1.AdmissionServiceGrpcKt
import com.acme.admissions.v1.batchGetAdmissionsRequest
import com.acme.careplans.v1.BrowseCarePlansRequestKt
import com.acme.careplans.v1.CarePlanQueryServiceGrpcKt
import com.acme.careplans.v1.browseCarePlansRequest
import com.acme.common.v1.pageRequest
import com.acme.deliveries.v1.ProofOfDeliveryWorkflowServiceGrpcKt
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.deliveries.v1.retryProofOfDeliveryWorkflowRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.admissions.v1.AdmissionService/BatchGetAdmissions and hands what it keeps to the sink of its kind. */
suspend fun carryGetAdmissionsToken(channel: ManagedChannel) {
    val upstream = AdmissionServiceGrpcKt.AdmissionServiceCoroutineStub(channel)
    val current = RpcBoundary.read { upstream.batchGetAdmissions(batchGetAdmissionsRequest { }) }
    val entry = CarryGetAdmissionsTokenRecord(
        nextPageToken = current.pageInfo.nextPageToken,
        correlationId = current.result.correlationId,
        message = current.result.message,
        status = current.result.status,
    )
    deliverCarryGetAdmissionsToken("proof-of-delivery-workflow", entry, channel)
}

/** Where carryGetAdmissionsToken hands each record. */
interface CarryGetAdmissionsTokenDestination {
    suspend fun transmit(rec: CarryGetAdmissionsTokenRecord)
}

/** What carryGetAdmissionsToken keeps of each BatchGetAdmissions response. */
data class CarryGetAdmissionsTokenRecord(val nextPageToken: String, val correlationId: String, val message: String, val status: String)

/** Hands records to acme.careplans.v1.CarePlanQueryService. */
class CarryGetAdmissionsTokenCarePlanQuery(private val stub: CarePlanQueryServiceGrpcKt.CarePlanQueryServiceCoroutineStub) : CarryGetAdmissionsTokenDestination {
    override suspend fun transmit(rec: CarryGetAdmissionsTokenRecord) {
        val request = browseCarePlansRequest {
            this.filter = BrowseCarePlansRequestKt.filter { this.region = rec.correlationId; this.query = rec.nextPageToken }
            this.page = pageRequest { this.pageToken = rec.message }
        }
        stub.browseCarePlans(request)
    }
}

/** Hands records to acme.deliveries.v1.ProofOfDeliveryWorkflowService. */
class CarryGetAdmissionsTokenProofOfDeliveryWorkflow(private val stub: ProofOfDeliveryWorkflowServiceGrpcKt.ProofOfDeliveryWorkflowServiceCoroutineStub) : CarryGetAdmissionsTokenDestination {
    override suspend fun transmit(rec: CarryGetAdmissionsTokenRecord) {
        val request = retryProofOfDeliveryWorkflowRequest {
            this.proofOfDelivery = proofOfDelivery { this.number = rec.nextPageToken }
            this.proofOfDeliveryId = rec.message
        }
        RpcBoundary.run("acme.deliveries.v1.ProofOfDeliveryWorkflowService/RetryProofOfDeliveryWorkflow") { stub.retryProofOfDeliveryWorkflow(request) }
    }
}

/** Only logs, for kinds no service takes yet. */
class CarryGetAdmissionsTokenLogOnly : CarryGetAdmissionsTokenDestination {
    override suspend fun transmit(rec: CarryGetAdmissionsTokenRecord) {
        LoggerFactory.getLogger("CarryGetAdmissionsTokenRecord").atInfo().addKeyValue("nextPageToken", rec.nextPageToken).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverCarryGetAdmissionsToken(kind: String, rec: CarryGetAdmissionsTokenRecord, channel: ManagedChannel) {
    var sink: CarryGetAdmissionsTokenDestination = CarryGetAdmissionsTokenLogOnly()
    when (kind) {
        "care-plan-query" -> sink = CarryGetAdmissionsTokenCarePlanQuery(CarePlanQueryServiceGrpcKt.CarePlanQueryServiceCoroutineStub(channel))
        "proof-of-delivery-workflow" -> sink = CarryGetAdmissionsTokenProofOfDeliveryWorkflow(ProofOfDeliveryWorkflowServiceGrpcKt.ProofOfDeliveryWorkflowServiceCoroutineStub(channel))
    }
    sink.transmit(rec)
}
