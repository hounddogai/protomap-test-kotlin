package com.acme.transport.ops.safetyexporter

import com.acme.drivers.v1.DriverLicenseReportingServiceGrpcKt
import com.acme.drivers.v1.countDriverLicenseTotalsRequest
import com.acme.payroll.v1.PayRunCalculationServiceGrpcKt
import com.acme.payroll.v1.calculatePayRunRequest
import com.acme.payroll.v1.payRun
import com.acme.routing.v1.RoutePlanWorkflowServiceGrpcKt
import com.acme.routing.v1.retryRoutePlanWorkflowRequest
import com.acme.transport.kit.text.normalize
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.routing.v1.RoutePlanWorkflowService/RetryRoutePlanWorkflow and hands what it keeps to the sink of its kind. */
suspend fun propagateRoutePlanWorkflowId(channel: ManagedChannel) {
    val source = RoutePlanWorkflowServiceGrpcKt.RoutePlanWorkflowServiceCoroutineStub(channel)
    val got = source.retryRoutePlanWorkflow(retryRoutePlanWorkflowRequest { this.reason = "main-propagate-route-plan-workflow-id" })
    val rec = PropagateRoutePlanWorkflowIdRecord(
        correlationId = got.result.correlationId,
        status = normalize(got.outcome.status),
    )
    dispatchPropagateRoutePlanWorkflowId("driver-license-reporting", rec, channel)
}

/** Where propagateRoutePlanWorkflowId hands each record. */
interface PropagateRoutePlanWorkflowIdSink {
    suspend fun write(rec: PropagateRoutePlanWorkflowIdRecord)
}

/** What propagateRoutePlanWorkflowId keeps of each RetryRoutePlanWorkflow response. */
data class PropagateRoutePlanWorkflowIdRecord(val correlationId: String, val status: String)

/** Passes records to acme.drivers.v1.DriverLicenseReportingService. */
class PropagateRoutePlanWorkflowIdDriverLicenseReporting(private val stub: DriverLicenseReportingServiceGrpcKt.DriverLicenseReportingServiceCoroutineStub) : PropagateRoutePlanWorkflowIdSink {
    override suspend fun write(rec: PropagateRoutePlanWorkflowIdRecord) {
        stub.countDriverLicenseTotals(countDriverLicenseTotalsRequest { this.orderBy = rec.status })
    }
}

/** Sends records to acme.payroll.v1.PayRunCalculationService. */
class PropagateRoutePlanWorkflowIdPayRunCalculation(private val stub: PayRunCalculationServiceGrpcKt.PayRunCalculationServiceCoroutineStub) : PropagateRoutePlanWorkflowIdSink {
    override suspend fun write(rec: PropagateRoutePlanWorkflowIdRecord) {
        stub.calculatePayRun(calculatePayRunRequest { this.payRun = payRun { this.payRunId = rec.status } })
    }
}

/** Only logs, for kinds no service takes yet. */
class PropagateRoutePlanWorkflowIdLogOnly : PropagateRoutePlanWorkflowIdSink {
    override suspend fun write(rec: PropagateRoutePlanWorkflowIdRecord) {
        LoggerFactory.getLogger("PropagateRoutePlanWorkflowIdRecord").atInfo().addKeyValue("correlationId", rec.correlationId).log("No sink for record")
    }
}

/** Picks the writer of a kind and runs it. */
suspend fun dispatchPropagateRoutePlanWorkflowId(kind: String, rec: PropagateRoutePlanWorkflowIdRecord, channel: ManagedChannel) {
    var write: suspend (PropagateRoutePlanWorkflowIdRecord) -> Unit = PropagateRoutePlanWorkflowIdLogOnly()::write
    when (kind) {
        "driver-license-reporting" -> write = PropagateRoutePlanWorkflowIdDriverLicenseReporting(DriverLicenseReportingServiceGrpcKt.DriverLicenseReportingServiceCoroutineStub(channel))::write
        "pay-run-calculation" -> write = PropagateRoutePlanWorkflowIdPayRunCalculation(PayRunCalculationServiceGrpcKt.PayRunCalculationServiceCoroutineStub(channel))::write
    }
    write(rec)
}
