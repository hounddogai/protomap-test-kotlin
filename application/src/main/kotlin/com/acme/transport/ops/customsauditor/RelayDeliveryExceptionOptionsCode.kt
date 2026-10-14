package com.acme.transport.ops.customsauditor

import com.acme.concessions.v1.ConcessionStandQueryServiceGrpcKt
import com.acme.concessions.v1.CountConcessionStandsRequestKt
import com.acme.concessions.v1.countConcessionStandsRequest
import com.acme.customers.v1.CustomerSegmentSyncServiceGrpcKt
import com.acme.customers.v1.getCustomerSegmentSyncStateRequest
import com.acme.deliveries.v1.DeliveryExceptionCalculationServiceGrpcKt
import com.acme.deliveries.v1.compareDeliveryExceptionOptionsRequest
import com.acme.maintenance.v1.ServiceScheduleWorkflowServiceGrpcKt
import com.acme.maintenance.v1.serviceSchedule
import com.acme.maintenance.v1.signalServiceScheduleWorkflowRequest
import com.acme.tours.v1.FindToursRequestKt
import com.acme.tours.v1.TourQueryServiceGrpcKt
import com.acme.tours.v1.findToursRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.deliveries.v1.DeliveryExceptionCalculationService/CompareDeliveryExceptionOptions and hands what it keeps to the sink of its kind. */
suspend fun relayDeliveryExceptionOptionsCode(channel: ManagedChannel) {
    val source = DeliveryExceptionCalculationServiceGrpcKt.DeliveryExceptionCalculationServiceCoroutineStub(channel)
    val reply = source.compareDeliveryExceptionOptions(compareDeliveryExceptionOptionsRequest { this.deliveryExceptionId = "primary-relay-delivery-exception-options-code" })
    val entry = RelayDeliveryExceptionOptionsCodeRecord(
        currencyCode = reply.breakdown.currencyCode,
        message = reply.result.message,
        correlationId = reply.result.correlationId,
    )
    dispatchRelayDeliveryExceptionOptionsCode("schedule-workflow", entry, channel)
}

/** Where relayDeliveryExceptionOptionsCode hands each record. */
interface RelayDeliveryExceptionOptionsCodeDestination {
    suspend fun write(rec: RelayDeliveryExceptionOptionsCodeRecord)
}

/** What relayDeliveryExceptionOptionsCode keeps of each CompareDeliveryExceptionOptions response. */
data class RelayDeliveryExceptionOptionsCodeRecord(val currencyCode: String, val message: String, val correlationId: String)

/** Writes records to acme.customers.v1.CustomerSegmentSyncService. */
class RelayDeliveryExceptionOptionsCodeCustomerSegmentSync(private val stub: CustomerSegmentSyncServiceGrpcKt.CustomerSegmentSyncServiceCoroutineStub) : RelayDeliveryExceptionOptionsCodeDestination {
    override suspend fun write(rec: RelayDeliveryExceptionOptionsCodeRecord) {
        stub.getCustomerSegmentSyncState(getCustomerSegmentSyncStateRequest { this.customerSegmentId = rec.correlationId })
    }
}

/** Hands records to acme.maintenance.v1.ServiceScheduleWorkflowService. */
class RelayDeliveryExceptionOptionsCodeServiceScheduleWorkflow(private val stub: ServiceScheduleWorkflowServiceGrpcKt.ServiceScheduleWorkflowServiceCoroutineStub) : RelayDeliveryExceptionOptionsCodeDestination {
    override suspend fun write(rec: RelayDeliveryExceptionOptionsCodeRecord) {
        val request = signalServiceScheduleWorkflowRequest {
            this.serviceSchedule = serviceSchedule { this.title = rec.currencyCode }
        }
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleWorkflowService/SignalServiceScheduleWorkflow") { stub.signalServiceScheduleWorkflow(request) }
    }
}

/** Hands records to acme.concessions.v1.ConcessionStandQueryService. */
class RelayDeliveryExceptionOptionsCodeConcessionStandQuery(private val stub: ConcessionStandQueryServiceGrpcKt.ConcessionStandQueryServiceCoroutineStub) : RelayDeliveryExceptionOptionsCodeDestination {
    override suspend fun write(rec: RelayDeliveryExceptionOptionsCodeRecord) {
        val request = countConcessionStandsRequest {
            this.filter = CountConcessionStandsRequestKt.filter { this.region = rec.correlationId; this.ownerId = rec.message }
        }
        stub.countConcessionStands(request)
    }
}

/** Sends records to acme.tours.v1.TourQueryService. */
class RelayDeliveryExceptionOptionsCodeTourQuery(private val stub: TourQueryServiceGrpcKt.TourQueryServiceCoroutineStub) : RelayDeliveryExceptionOptionsCodeDestination {
    override suspend fun write(rec: RelayDeliveryExceptionOptionsCodeRecord) {
        val request = findToursRequest {
            this.filter = FindToursRequestKt.filter { this.ownerId = rec.correlationId; this.query = rec.message; this.region = rec.currencyCode }
        }
        stub.findTours(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class RelayDeliveryExceptionOptionsCodeLogOnly : RelayDeliveryExceptionOptionsCodeDestination {
    override suspend fun write(rec: RelayDeliveryExceptionOptionsCodeRecord) {
        LoggerFactory.getLogger("RelayDeliveryExceptionOptionsCodeRecord").atInfo().addKeyValue("currencyCode", rec.currencyCode).log("No sink for record")
    }
}

/** Picks the writer of a kind and runs it. */
suspend fun dispatchRelayDeliveryExceptionOptionsCode(kind: String, rec: RelayDeliveryExceptionOptionsCodeRecord, channel: ManagedChannel) {
    var write: suspend (RelayDeliveryExceptionOptionsCodeRecord) -> Unit = RelayDeliveryExceptionOptionsCodeLogOnly()::write
    when (kind) {
        "customer-segment-sync" -> write = RelayDeliveryExceptionOptionsCodeCustomerSegmentSync(CustomerSegmentSyncServiceGrpcKt.CustomerSegmentSyncServiceCoroutineStub(channel))::write
        "schedule-workflow" -> write = RelayDeliveryExceptionOptionsCodeServiceScheduleWorkflow(ServiceScheduleWorkflowServiceGrpcKt.ServiceScheduleWorkflowServiceCoroutineStub(channel))::write
        "concession-stand-query" -> write = RelayDeliveryExceptionOptionsCodeConcessionStandQuery(ConcessionStandQueryServiceGrpcKt.ConcessionStandQueryServiceCoroutineStub(channel))::write
        "tour-query" -> write = RelayDeliveryExceptionOptionsCodeTourQuery(TourQueryServiceGrpcKt.TourQueryServiceCoroutineStub(channel))::write
    }
    write(rec)
}
