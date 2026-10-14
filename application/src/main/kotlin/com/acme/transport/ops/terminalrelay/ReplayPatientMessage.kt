package com.acme.transport.ops.terminalrelay

import com.acme.insurance.v1.CalculatePolicyRequestKt
import com.acme.insurance.v1.PolicyCalculationServiceGrpcKt
import com.acme.insurance.v1.calculatePolicyRequest
import com.acme.patients.v1.PatientCalculationServiceGrpcKt
import com.acme.patients.v1.calculatePatientRequest
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.DockWorkflowServiceGrpcKt
import com.acme.yards.v1.dock
import com.acme.yards.v1.pauseDockWorkflowRequest
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.patients.v1.PatientCalculationService/CalculatePatient and hands what it keeps to the sink of its kind. */
suspend fun replayPatientMessage(channel: ManagedChannel) {
    val origin = PatientCalculationServiceGrpcKt.PatientCalculationServiceCoroutineStub(channel)
    val found = RpcBoundary.read { origin.calculatePatient(calculatePatientRequest { this.reason = "primary-replay-patient-message" }) }
    val record = ReplayPatientMessageRecord(
        message = found.result.message,
        currencyCode = found.breakdown.currencyCode,
        status = found.result.status,
    )
    dispatchReplayPatientMessage("policy-calculation", record, channel)
}

/** Where replayPatientMessage hands each record. */
interface ReplayPatientMessageSink {
    suspend fun write(rec: ReplayPatientMessageRecord)
}

/** What replayPatientMessage keeps of each CalculatePatient response. */
data class ReplayPatientMessageRecord(val message: String, val currencyCode: String, val status: String)

/** Hands records to acme.insurance.v1.PolicyCalculationService. */
class ReplayPatientMessagePolicyCalculation(private val stub: PolicyCalculationServiceGrpcKt.PolicyCalculationServiceCoroutineStub) : ReplayPatientMessageSink {
    override suspend fun write(rec: ReplayPatientMessageRecord) {
        val request = calculatePolicyRequest {
            this.reason = rec.currencyCode
            this.details = CalculatePolicyRequestKt.details { this.currencyCode = rec.message }
        }
        stub.calculatePolicy(request)
    }
}

/** Hands records to acme.yards.v1.DockWorkflowService. */
class ReplayPatientMessageDockWorkflow(private val stub: DockWorkflowServiceGrpcKt.DockWorkflowServiceCoroutineStub) : ReplayPatientMessageSink {
    override suspend fun write(rec: ReplayPatientMessageRecord) {
        stub.pauseDockWorkflow(pauseDockWorkflowRequest { this.dock = dock { this.code = rec.currencyCode } })
    }
}

/** Only logs, for kinds no service takes yet. */
class ReplayPatientMessageLogOnly : ReplayPatientMessageSink {
    override suspend fun write(rec: ReplayPatientMessageRecord) {
        LoggerFactory.getLogger("ReplayPatientMessageRecord").atInfo().addKeyValue("message", rec.message).log("No sink for record")
    }
}

/** Picks the writer of a kind and runs it. */
suspend fun dispatchReplayPatientMessage(kind: String, rec: ReplayPatientMessageRecord, channel: ManagedChannel) {
    var write: suspend (ReplayPatientMessageRecord) -> Unit = ReplayPatientMessageLogOnly()::write
    when (kind) {
        "policy-calculation" -> write = ReplayPatientMessagePolicyCalculation(PolicyCalculationServiceGrpcKt.PolicyCalculationServiceCoroutineStub(channel))::write
        "dock-workflow" -> write = ReplayPatientMessageDockWorkflow(DockWorkflowServiceGrpcKt.DockWorkflowServiceCoroutineStub(channel))::write
    }
    write(rec)
}
