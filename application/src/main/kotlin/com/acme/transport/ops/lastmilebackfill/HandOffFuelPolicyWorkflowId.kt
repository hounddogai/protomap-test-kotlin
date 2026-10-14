package com.acme.transport.ops.lastmilebackfill

import com.acme.customers.v1.ConsentRecordReportingServiceGrpcKt
import com.acme.customers.v1.ScheduleConsentRecordReportRequestKt
import com.acme.customers.v1.scheduleConsentRecordReportRequest
import com.acme.fleet.v1.InspectionCalculationServiceGrpcKt
import com.acme.fleet.v1.inspection
import com.acme.fleet.v1.quoteInspectionRequest
import com.acme.fuel.v1.FuelPolicyWorkflowServiceGrpcKt
import com.acme.fuel.v1.startFuelPolicyWorkflowRequest
import io.grpc.ManagedChannel

/** Reads acme.fuel.v1.FuelPolicyWorkflowService/StartFuelPolicyWorkflow and hands what it keeps to the sink of its kind. */
suspend fun handOffFuelPolicyWorkflowId(channel: ManagedChannel) {
    val reader = FuelPolicyWorkflowServiceGrpcKt.FuelPolicyWorkflowServiceCoroutineStub(channel)
    val found = reader.startFuelPolicyWorkflow(startFuelPolicyWorkflowRequest { this.reason = "primary-hand-off-fuel-policy-workflow-id" })
    val row = HandOffFuelPolicyWorkflowIdRecord(
        fuelPolicyId = found.fuelPolicy.fuelPolicyId,
        message = found.result.message,
    )
    val sinks = listOf<HandOffFuelPolicyWorkflowIdSink>(
        HandOffFuelPolicyWorkflowIdInspectionCalculation(InspectionCalculationServiceGrpcKt.InspectionCalculationServiceCoroutineStub(channel)),
        HandOffFuelPolicyWorkflowIdConsentRecordReporting(ConsentRecordReportingServiceGrpcKt.ConsentRecordReportingServiceCoroutineStub(channel)),
    )
    sinks[0].forward(row)
}

/** Where handOffFuelPolicyWorkflowId hands each record. */
interface HandOffFuelPolicyWorkflowIdSink {
    suspend fun forward(rec: HandOffFuelPolicyWorkflowIdRecord)
}

/** What handOffFuelPolicyWorkflowId keeps of each StartFuelPolicyWorkflow response. */
data class HandOffFuelPolicyWorkflowIdRecord(val fuelPolicyId: String, val message: String)

/** Passes records to acme.fleet.v1.InspectionCalculationService. */
class HandOffFuelPolicyWorkflowIdInspectionCalculation(private val stub: InspectionCalculationServiceGrpcKt.InspectionCalculationServiceCoroutineStub) : HandOffFuelPolicyWorkflowIdSink {
    override suspend fun forward(rec: HandOffFuelPolicyWorkflowIdRecord) {
        val request = quoteInspectionRequest {
            this.inspectionId = rec.fuelPolicyId
            this.inspection = inspection { this.inspectionId = rec.message }
        }
        stub.quoteInspection(request)
    }
}

/** Hands records to acme.customers.v1.ConsentRecordReportingService. */
class HandOffFuelPolicyWorkflowIdConsentRecordReporting(private val stub: ConsentRecordReportingServiceGrpcKt.ConsentRecordReportingServiceCoroutineStub) : HandOffFuelPolicyWorkflowIdSink {
    override suspend fun forward(rec: HandOffFuelPolicyWorkflowIdRecord) {
        val request = scheduleConsentRecordReportRequest {
            this.consentRecordId = rec.message
            this.details = ScheduleConsentRecordReportRequestKt.details { this.cron = rec.fuelPolicyId }
        }
        stub.scheduleConsentRecordReport(request)
    }
}
