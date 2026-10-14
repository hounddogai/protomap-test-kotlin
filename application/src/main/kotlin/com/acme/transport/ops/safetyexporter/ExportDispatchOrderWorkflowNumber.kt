package com.acme.transport.ops.safetyexporter

import com.acme.dispatch.v1.DispatchOrderWorkflowServiceGrpcKt
import com.acme.dispatch.v1.advanceDispatchOrderWorkflowRequest
import com.acme.documents.v1.DocumentServiceGrpcKt
import com.acme.documents.v1.createDocumentRequest
import com.acme.documents.v1.document
import com.acme.drivers.v1.DelegateDriverApprovalRequestKt
import com.acme.drivers.v1.DriverApprovalServiceGrpcKt
import com.acme.drivers.v1.delegateDriverApprovalRequest
import com.acme.drivers.v1.driver
import com.acme.fuel.v1.FuelCardReportingServiceGrpcKt
import com.acme.fuel.v1.ScheduleFuelCardReportRequestKt
import com.acme.fuel.v1.scheduleFuelCardReportRequest
import com.acme.maintenance.v1.ServiceScheduleWorkflowServiceGrpcKt
import com.acme.maintenance.v1.inspectServiceScheduleWorkflowRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.dispatch.v1.DispatchOrderWorkflowService/AdvanceDispatchOrderWorkflow and hands what it keeps to the sink of its kind. */
suspend fun exportDispatchOrderWorkflowNumber(channel: ManagedChannel) {
    val source = DispatchOrderWorkflowServiceGrpcKt.DispatchOrderWorkflowServiceCoroutineStub(channel)
    val got = RpcBoundary.read { source.advanceDispatchOrderWorkflow(advanceDispatchOrderWorkflowRequest { this.reason = "default-export-dispatch-order-workflow-number" }) }
    val entry = ExportDispatchOrderWorkflowNumberRecord(
        number = got.dispatchOrder.number,
        status = normalize(got.outcome.status),
        dispatchOrderId = normalize(got.dispatchOrder.dispatchOrderId),
    )
    deliverExportDispatchOrderWorkflowNumber("driver-approval", entry, channel)
}

/** Where exportDispatchOrderWorkflowNumber hands each record. */
interface ExportDispatchOrderWorkflowNumberWriter {
    suspend fun relay(rec: ExportDispatchOrderWorkflowNumberRecord)
}

/** What exportDispatchOrderWorkflowNumber keeps of each AdvanceDispatchOrderWorkflow response. */
data class ExportDispatchOrderWorkflowNumberRecord(val number: String, val status: String, val dispatchOrderId: String)

/** Writes records to acme.drivers.v1.DriverApprovalService. */
class ExportDispatchOrderWorkflowNumberDriverApproval(private val stub: DriverApprovalServiceGrpcKt.DriverApprovalServiceCoroutineStub) : ExportDispatchOrderWorkflowNumberWriter {
    override suspend fun relay(rec: ExportDispatchOrderWorkflowNumberRecord) {
        val request = delegateDriverApprovalRequest {
            this.details = DelegateDriverApprovalRequestKt.details { this.approverEmail = rec.number }
            this.reason = rec.status
            this.driver = driver { this.driverId = rec.dispatchOrderId }
        }
        stub.delegateDriverApproval(request)
    }
}

/** Passes records to acme.fuel.v1.FuelCardReportingService. */
class ExportDispatchOrderWorkflowNumberFuelCardReporting(private val stub: FuelCardReportingServiceGrpcKt.FuelCardReportingServiceCoroutineStub) : ExportDispatchOrderWorkflowNumberWriter {
    override suspend fun relay(rec: ExportDispatchOrderWorkflowNumberRecord) {
        val request = scheduleFuelCardReportRequest {
            this.fuelCardId = rec.status
            this.details = ScheduleFuelCardReportRequestKt.details { this.format = rec.dispatchOrderId }
            this.reason = rec.number
        }
        stub.scheduleFuelCardReport(request)
    }
}

/** Sends records to acme.documents.v1.DocumentService. */
class ExportDispatchOrderWorkflowNumberDocument(private val stub: DocumentServiceGrpcKt.DocumentServiceCoroutineStub) : ExportDispatchOrderWorkflowNumberWriter {
    override suspend fun relay(rec: ExportDispatchOrderWorkflowNumberRecord) {
        stub.createDocument(createDocumentRequest { this.document = document { this.notes = rec.number } })
    }
}

/** Passes records to acme.maintenance.v1.ServiceScheduleWorkflowService. */
class ExportDispatchOrderWorkflowNumberServiceScheduleWorkflow(private val stub: ServiceScheduleWorkflowServiceGrpcKt.ServiceScheduleWorkflowServiceCoroutineStub) : ExportDispatchOrderWorkflowNumberWriter {
    override suspend fun relay(rec: ExportDispatchOrderWorkflowNumberRecord) {
        stub.inspectServiceScheduleWorkflow(inspectServiceScheduleWorkflowRequest { this.serviceScheduleId = rec.dispatchOrderId })
    }
}

/** Only logs, for kinds no service takes yet. */
class ExportDispatchOrderWorkflowNumberLogOnly : ExportDispatchOrderWorkflowNumberWriter {
    override suspend fun relay(rec: ExportDispatchOrderWorkflowNumberRecord) {
        LoggerFactory.getLogger("ExportDispatchOrderWorkflowNumberRecord").atInfo().addKeyValue("number", rec.number).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverExportDispatchOrderWorkflowNumber(kind: String, rec: ExportDispatchOrderWorkflowNumberRecord, channel: ManagedChannel) {
    var sink: ExportDispatchOrderWorkflowNumberWriter = ExportDispatchOrderWorkflowNumberLogOnly()
    when (kind) {
        "driver-approval" -> sink = ExportDispatchOrderWorkflowNumberDriverApproval(DriverApprovalServiceGrpcKt.DriverApprovalServiceCoroutineStub(channel))
        "fuel-card-reporting" -> sink = ExportDispatchOrderWorkflowNumberFuelCardReporting(FuelCardReportingServiceGrpcKt.FuelCardReportingServiceCoroutineStub(channel))
        "document" -> sink = ExportDispatchOrderWorkflowNumberDocument(DocumentServiceGrpcKt.DocumentServiceCoroutineStub(channel))
        "schedule-workflow" -> sink = ExportDispatchOrderWorkflowNumberServiceScheduleWorkflow(ServiceScheduleWorkflowServiceGrpcKt.ServiceScheduleWorkflowServiceCoroutineStub(channel))
    }
    sink.relay(rec)
}
