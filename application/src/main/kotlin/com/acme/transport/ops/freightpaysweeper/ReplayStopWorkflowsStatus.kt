package com.acme.transport.ops.freightpaysweeper

import com.acme.budgeting.v1.BudgetCalculationServiceGrpcKt
import com.acme.budgeting.v1.SimulateBudgetRequestKt
import com.acme.budgeting.v1.simulateBudgetRequest
import com.acme.customers.v1.GenerateHouseholdReportRequestKt
import com.acme.customers.v1.HouseholdReportingServiceGrpcKt
import com.acme.customers.v1.generateHouseholdReportRequest
import com.acme.drivers.v1.DriverLicenseWorkflowServiceGrpcKt
import com.acme.drivers.v1.SignalDriverLicenseWorkflowRequestKt
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.signalDriverLicenseWorkflowRequest
import com.acme.routing.v1.StopWorkflowServiceGrpcKt
import com.acme.routing.v1.listStopWorkflowsRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.routing.v1.StopWorkflowService/ListStopWorkflows and hands what it keeps to the sink of its kind. */
suspend fun replayStopWorkflowsStatus(channel: ManagedChannel) {
    val origin = StopWorkflowServiceGrpcKt.StopWorkflowServiceCoroutineStub(channel)
    val found = RpcBoundary.read { origin.listStopWorkflows(listStopWorkflowsRequest { this.orderBy = "live-replay-stop-workflows-status" }) }
    val rec = ReplayStopWorkflowsStatusRecord(
        status = normalize(found.result.status),
        correlationId = found.result.correlationId,
        message = found.result.message,
        nextPageToken = normalize(found.pageInfo.nextPageToken),
    )
    val sinks = mapOf<String, ReplayStopWorkflowsStatusTarget>(
        "household-reporting" to ReplayStopWorkflowsStatusHouseholdReporting(HouseholdReportingServiceGrpcKt.HouseholdReportingServiceCoroutineStub(channel)),
        "budget-calculation" to ReplayStopWorkflowsStatusBudgetCalculation(BudgetCalculationServiceGrpcKt.BudgetCalculationServiceCoroutineStub(channel)),
        "driver-license-workflow" to ReplayStopWorkflowsStatusDriverLicenseWorkflow(DriverLicenseWorkflowServiceGrpcKt.DriverLicenseWorkflowServiceCoroutineStub(channel)),
    )
    routeReplayStopWorkflowsStatus(sinks, "household-reporting", rec)
}

/** Where replayStopWorkflowsStatus hands each record. */
interface ReplayStopWorkflowsStatusTarget {
    suspend fun hand(rec: ReplayStopWorkflowsStatusRecord)
}

/** What replayStopWorkflowsStatus keeps of each ListStopWorkflows response. */
data class ReplayStopWorkflowsStatusRecord(val status: String, val correlationId: String, val message: String, val nextPageToken: String)

/** Sends records to acme.customers.v1.HouseholdReportingService. */
class ReplayStopWorkflowsStatusHouseholdReporting(private val stub: HouseholdReportingServiceGrpcKt.HouseholdReportingServiceCoroutineStub) : ReplayStopWorkflowsStatusTarget {
    override suspend fun hand(rec: ReplayStopWorkflowsStatusRecord) {
        val request = generateHouseholdReportRequest {
            this.reason = rec.nextPageToken
            this.details = GenerateHouseholdReportRequestKt.details { this.cron = rec.correlationId; this.format = rec.status }
        }
        RpcBoundary.run("acme.customers.v1.HouseholdReportingService/GenerateHouseholdReport") { stub.generateHouseholdReport(request) }
    }
}

/** Hands records to acme.budgeting.v1.BudgetCalculationService. */
class ReplayStopWorkflowsStatusBudgetCalculation(private val stub: BudgetCalculationServiceGrpcKt.BudgetCalculationServiceCoroutineStub) : ReplayStopWorkflowsStatusTarget {
    override suspend fun hand(rec: ReplayStopWorkflowsStatusRecord) {
        val request = simulateBudgetRequest {
            this.details = SimulateBudgetRequestKt.details { this.currencyCode = rec.correlationId }
        }
        RpcBoundary.run("acme.budgeting.v1.BudgetCalculationService/SimulateBudget") { stub.simulateBudget(request) }
    }
}

/** Writes records to acme.drivers.v1.DriverLicenseWorkflowService. */
class ReplayStopWorkflowsStatusDriverLicenseWorkflow(private val stub: DriverLicenseWorkflowServiceGrpcKt.DriverLicenseWorkflowServiceCoroutineStub) : ReplayStopWorkflowsStatusTarget {
    override suspend fun hand(rec: ReplayStopWorkflowsStatusRecord) {
        val request = signalDriverLicenseWorkflowRequest {
            this.driverLicense = driverLicense { this.driverLicenseId = rec.correlationId }
            this.details = SignalDriverLicenseWorkflowRequestKt.details { this.step = rec.nextPageToken }
            this.driverLicenseId = rec.status
        }
        stub.signalDriverLicenseWorkflow(request)
    }
}

/** Hands a record to the sink registered for its kind. */
suspend fun routeReplayStopWorkflowsStatus(sinks: Map<String, ReplayStopWorkflowsStatusTarget>, kind: String, rec: ReplayStopWorkflowsStatusRecord) {
    val sink = sinks[kind] ?: error("No sink registered for $kind")
    sink.hand(rec)
}
