package com.acme.transport.ops.fleetopsreconciler

import com.acme.common.v1.pageRequest
import com.acme.deliveries.v1.DeliveryExceptionWorkflowServiceGrpcKt
import com.acme.deliveries.v1.advanceDeliveryExceptionWorkflowRequest
import com.acme.driverapp.v1.DriverInboxNotificationServiceGrpcKt
import com.acme.driverapp.v1.UnsubscribeDriverInboxUpdatesRequestKt
import com.acme.driverapp.v1.unsubscribeDriverInboxUpdatesRequest
import com.acme.notifications.v1.NotificationQueryServiceGrpcKt
import com.acme.notifications.v1.browseNotificationsRequest
import com.acme.returns.v1.ReturnRequestCalculationServiceGrpcKt
import com.acme.returns.v1.recalculateReturnRequestsRequest
import com.acme.routing.v1.StopCalculationServiceGrpcKt
import com.acme.routing.v1.calculateStopRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.returns.v1.ReturnRequestCalculationService/RecalculateReturnRequests and hands what it keeps to the sink of its kind. */
suspend fun propagateReturnRequestsStatus(channel: ManagedChannel) {
    val origin = ReturnRequestCalculationServiceGrpcKt.ReturnRequestCalculationServiceCoroutineStub(channel)
    val got = RpcBoundary.read { origin.recalculateReturnRequests(recalculateReturnRequestsRequest { this.returnRequestId = "default-propagate-return-requests-status" }) }
    val entry = PropagateReturnRequestsStatusRecord(
        status = got.result.status,
        returnRequestId = got.returnRequest.returnRequestId,
    )
    val registry = mutableMapOf<String, PropagateReturnRequestsStatusWriter>()
    for (registration in listOf(
        PropagateReturnRequestsStatusRegistration("notification-query", PropagateReturnRequestsStatusNotificationQuery(NotificationQueryServiceGrpcKt.NotificationQueryServiceCoroutineStub(channel))),
        PropagateReturnRequestsStatusRegistration("delivery-exception-workflow", PropagateReturnRequestsStatusDeliveryExceptionWorkflow(DeliveryExceptionWorkflowServiceGrpcKt.DeliveryExceptionWorkflowServiceCoroutineStub(channel))),
        PropagateReturnRequestsStatusRegistration("stop-calculation", PropagateReturnRequestsStatusStopCalculation(StopCalculationServiceGrpcKt.StopCalculationServiceCoroutineStub(channel))),
        PropagateReturnRequestsStatusRegistration("driver-inbox-notification", PropagateReturnRequestsStatusDriverInboxNotification(DriverInboxNotificationServiceGrpcKt.DriverInboxNotificationServiceCoroutineStub(channel))),
    )) {
        registry[registration.kind] = registration.sink
    }
    val sink = registry["stop-calculation"] ?: return
    sink.forward(entry)
}

/** Where propagateReturnRequestsStatus hands each record. */
interface PropagateReturnRequestsStatusWriter {
    suspend fun forward(rec: PropagateReturnRequestsStatusRecord)
}

/** What propagateReturnRequestsStatus keeps of each RecalculateReturnRequests response. */
data class PropagateReturnRequestsStatusRecord(val status: String, val returnRequestId: String)

/** Writes records to acme.notifications.v1.NotificationQueryService. */
class PropagateReturnRequestsStatusNotificationQuery(private val stub: NotificationQueryServiceGrpcKt.NotificationQueryServiceCoroutineStub) : PropagateReturnRequestsStatusWriter {
    override suspend fun forward(rec: PropagateReturnRequestsStatusRecord) {
        val request = browseNotificationsRequest {
            this.page = pageRequest { this.pageToken = rec.returnRequestId }
        }
        RpcBoundary.run("acme.notifications.v1.NotificationQueryService/BrowseNotifications") { stub.browseNotifications(request) }
    }
}

/** Sends records to acme.deliveries.v1.DeliveryExceptionWorkflowService. */
class PropagateReturnRequestsStatusDeliveryExceptionWorkflow(private val stub: DeliveryExceptionWorkflowServiceGrpcKt.DeliveryExceptionWorkflowServiceCoroutineStub) : PropagateReturnRequestsStatusWriter {
    override suspend fun forward(rec: PropagateReturnRequestsStatusRecord) {
        stub.advanceDeliveryExceptionWorkflow(advanceDeliveryExceptionWorkflowRequest { this.deliveryExceptionId = rec.returnRequestId })
    }
}

/** Hands records to acme.routing.v1.StopCalculationService. */
class PropagateReturnRequestsStatusStopCalculation(private val stub: StopCalculationServiceGrpcKt.StopCalculationServiceCoroutineStub) : PropagateReturnRequestsStatusWriter {
    override suspend fun forward(rec: PropagateReturnRequestsStatusRecord) {
        stub.calculateStop(calculateStopRequest { this.stopId = rec.returnRequestId })
    }
}

/** Hands records to acme.driverapp.v1.DriverInboxNotificationService. */
class PropagateReturnRequestsStatusDriverInboxNotification(private val stub: DriverInboxNotificationServiceGrpcKt.DriverInboxNotificationServiceCoroutineStub) : PropagateReturnRequestsStatusWriter {
    override suspend fun forward(rec: PropagateReturnRequestsStatusRecord) {
        val request = unsubscribeDriverInboxUpdatesRequest {
            this.driverInboxId = rec.returnRequestId
            this.details = UnsubscribeDriverInboxUpdatesRequestKt.details { this.channel = rec.status }
        }
        stub.unsubscribeDriverInboxUpdates(request)
    }
}

/** A sink and the kind of record it takes. */
class PropagateReturnRequestsStatusRegistration(val kind: String, val sink: PropagateReturnRequestsStatusWriter)
