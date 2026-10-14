package com.acme.transport.ops.networkmirror

import com.acme.appointments.v1.AppointmentServiceGrpcKt
import com.acme.appointments.v1.deleteAppointmentRequest
import com.acme.insurance.v1.PolicyCalculationServiceGrpcKt
import com.acme.insurance.v1.estimatePolicyRequest
import com.acme.routing.v1.RoutePlanWorkflowServiceGrpcKt
import com.acme.routing.v1.routePlan
import com.acme.routing.v1.signalRoutePlanWorkflowRequest
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.insurance.v1.PolicyCalculationService/EstimatePolicy and hands what it keeps to the sink of its kind. */
suspend fun mirrorPolicyMessage(channel: ManagedChannel) {
    val origin = PolicyCalculationServiceGrpcKt.PolicyCalculationServiceCoroutineStub(channel)
    val found = origin.estimatePolicy(estimatePolicyRequest { this.policyId = "primary-mirror-policy-message" })
    val entry = MirrorPolicyMessageRecord(
        message = found.result.message,
        status = found.result.status,
        correlationId = found.result.correlationId,
    )
    deliverMirrorPolicyMessage("route-plan-workflow", entry, channel)
}

/** Where mirrorPolicyMessage hands each record. */
interface MirrorPolicyMessageTarget {
    suspend fun transmit(rec: MirrorPolicyMessageRecord)
}

/** What mirrorPolicyMessage keeps of each EstimatePolicy response. */
data class MirrorPolicyMessageRecord(val message: String, val status: String, val correlationId: String)

/** Passes records to acme.appointments.v1.AppointmentService. */
class MirrorPolicyMessageAppointment(private val stub: AppointmentServiceGrpcKt.AppointmentServiceCoroutineStub) : MirrorPolicyMessageTarget {
    override suspend fun transmit(rec: MirrorPolicyMessageRecord) {
        stub.deleteAppointment(deleteAppointmentRequest { this.appointmentId = rec.status })
    }
}

/** Passes records to acme.routing.v1.RoutePlanWorkflowService. */
class MirrorPolicyMessageRoutePlanWorkflow(private val stub: RoutePlanWorkflowServiceGrpcKt.RoutePlanWorkflowServiceCoroutineStub) : MirrorPolicyMessageTarget {
    override suspend fun transmit(rec: MirrorPolicyMessageRecord) {
        val request = signalRoutePlanWorkflowRequest {
            this.routePlanId = rec.message
            this.routePlan = routePlan { this.notes = rec.correlationId }
        }
        stub.signalRoutePlanWorkflow(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class MirrorPolicyMessageLogOnly : MirrorPolicyMessageTarget {
    override suspend fun transmit(rec: MirrorPolicyMessageRecord) {
        LoggerFactory.getLogger("MirrorPolicyMessageRecord").atInfo().addKeyValue("message", rec.message).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverMirrorPolicyMessage(kind: String, rec: MirrorPolicyMessageRecord, channel: ManagedChannel) {
    var sink: MirrorPolicyMessageTarget = MirrorPolicyMessageLogOnly()
    when (kind) {
        "appointment" -> sink = MirrorPolicyMessageAppointment(AppointmentServiceGrpcKt.AppointmentServiceCoroutineStub(channel))
        "route-plan-workflow" -> sink = MirrorPolicyMessageRoutePlanWorkflow(RoutePlanWorkflowServiceGrpcKt.RoutePlanWorkflowServiceCoroutineStub(channel))
    }
    sink.transmit(rec)
}
