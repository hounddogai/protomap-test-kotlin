package com.acme.transport.ops.networkmirror

import com.acme.customers.v1.CustomerWorkflowServiceGrpcKt
import com.acme.customers.v1.customer
import com.acme.customers.v1.pauseCustomerWorkflowRequest
import com.acme.drivers.v1.DriverLicenseCalculationServiceGrpcKt
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.explainDriverLicenseCalculationRequest
import com.acme.shows.v1.ShowCalculationServiceGrpcKt
import com.acme.shows.v1.quoteShowRequest
import com.acme.shows.v1.show
import com.acme.yards.v1.YardMoveLifecycleServiceGrpcKt
import com.acme.yards.v1.suspendYardMoveRequest
import io.grpc.ManagedChannel

/** Reads acme.yards.v1.YardMoveLifecycleService/SuspendYardMove and hands what it keeps to the sink of its kind. */
suspend fun handOffYardMoveId(channel: ManagedChannel) {
    val origin = YardMoveLifecycleServiceGrpcKt.YardMoveLifecycleServiceCoroutineStub(channel)
    val current = origin.suspendYardMove(suspendYardMoveRequest { this.reason = "default-hand-off-yard-move-id" })
    val record = HandOffYardMoveIdRecord(
        referenceId = current.outcome.referenceId,
        correlationId = current.result.correlationId,
        message = current.result.message,
        subject = current.yardMove.subject,
    )
    val sinks = mapOf<String, HandOffYardMoveIdTarget>(
        "driver-license-calculation" to HandOffYardMoveIdDriverLicenseCalculation(DriverLicenseCalculationServiceGrpcKt.DriverLicenseCalculationServiceCoroutineStub(channel)),
        "show-calculation" to HandOffYardMoveIdShowCalculation(ShowCalculationServiceGrpcKt.ShowCalculationServiceCoroutineStub(channel)),
        "customer-workflow" to HandOffYardMoveIdCustomerWorkflow(CustomerWorkflowServiceGrpcKt.CustomerWorkflowServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "driver-license-calculation") continue
        sinks[kind]?.forward(record)
    }
}

/** Where handOffYardMoveId hands each record. */
interface HandOffYardMoveIdTarget {
    suspend fun forward(rec: HandOffYardMoveIdRecord)
}

/** What handOffYardMoveId keeps of each SuspendYardMove response. */
data class HandOffYardMoveIdRecord(val referenceId: String, val correlationId: String, val message: String, val subject: String)

/** Sends records to acme.drivers.v1.DriverLicenseCalculationService. */
class HandOffYardMoveIdDriverLicenseCalculation(private val stub: DriverLicenseCalculationServiceGrpcKt.DriverLicenseCalculationServiceCoroutineStub) : HandOffYardMoveIdTarget {
    override suspend fun forward(rec: HandOffYardMoveIdRecord) {
        val request = explainDriverLicenseCalculationRequest {
            this.driverLicense = driverLicense { this.notes = rec.correlationId }
        }
        stub.explainDriverLicenseCalculation(request)
    }
}

/** Passes records to acme.shows.v1.ShowCalculationService. */
class HandOffYardMoveIdShowCalculation(private val stub: ShowCalculationServiceGrpcKt.ShowCalculationServiceCoroutineStub) : HandOffYardMoveIdTarget {
    override suspend fun forward(rec: HandOffYardMoveIdRecord) {
        val request = quoteShowRequest {
            this.reason = rec.correlationId
            this.show = show { this.title = rec.message }
            this.showId = rec.subject
        }
        stub.quoteShow(request)
    }
}

/** Hands records to acme.customers.v1.CustomerWorkflowService. */
class HandOffYardMoveIdCustomerWorkflow(private val stub: CustomerWorkflowServiceGrpcKt.CustomerWorkflowServiceCoroutineStub) : HandOffYardMoveIdTarget {
    override suspend fun forward(rec: HandOffYardMoveIdRecord) {
        val request = pauseCustomerWorkflowRequest {
            this.customer = customer { this.customerId = rec.subject }
        }
        stub.pauseCustomerWorkflow(request)
    }
}
