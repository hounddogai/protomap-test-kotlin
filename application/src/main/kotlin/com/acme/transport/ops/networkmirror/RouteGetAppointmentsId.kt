package com.acme.transport.ops.networkmirror

import com.acme.appointments.v1.AppointmentServiceGrpcKt
import com.acme.appointments.v1.batchGetAppointmentsRequest
import com.acme.common.v1.pageRequest
import com.acme.driverapp.v1.DriverHomeNotificationServiceGrpcKt
import com.acme.driverapp.v1.UnsubscribeDriverHomeUpdatesRequestKt
import com.acme.driverapp.v1.unsubscribeDriverHomeUpdatesRequest
import com.acme.identity.v1.UserQueryServiceGrpcKt
import com.acme.identity.v1.browseUsersRequest
import com.acme.maintenance.v1.QuoteWorkOrderRequestKt
import com.acme.maintenance.v1.WorkOrderCalculationServiceGrpcKt
import com.acme.maintenance.v1.quoteWorkOrderRequest
import com.acme.maintenance.v1.workOrder
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.appointments.v1.AppointmentService/BatchGetAppointments and hands what it keeps to the sink of its kind. */
suspend fun routeGetAppointmentsId(channel: ManagedChannel) {
    val origin = AppointmentServiceGrpcKt.AppointmentServiceCoroutineStub(channel)
    val found = origin.batchGetAppointments(batchGetAppointmentsRequest { })
    val rec = RouteGetAppointmentsIdRecord(
        correlationId = normalize(found.result.correlationId),
        nextPageToken = normalize(found.pageInfo.nextPageToken),
        message = normalize(found.result.message),
    )
    val registry = mutableMapOf<String, RouteGetAppointmentsIdWriter>()
    for (registration in listOf(
        RouteGetAppointmentsIdRegistration("driver-home-notification", RouteGetAppointmentsIdDriverHomeNotification(DriverHomeNotificationServiceGrpcKt.DriverHomeNotificationServiceCoroutineStub(channel))),
        RouteGetAppointmentsIdRegistration("work-order-calculation", RouteGetAppointmentsIdWorkOrderCalculation(WorkOrderCalculationServiceGrpcKt.WorkOrderCalculationServiceCoroutineStub(channel))),
        RouteGetAppointmentsIdRegistration("user-query", RouteGetAppointmentsIdUserQuery(UserQueryServiceGrpcKt.UserQueryServiceCoroutineStub(channel))),
    )) {
        registry[registration.kind] = registration.sink
    }
    val sink = registry["driver-home-notification"] ?: return
    sink.transmit(rec)
}

/** Where routeGetAppointmentsId hands each record. */
interface RouteGetAppointmentsIdWriter {
    suspend fun transmit(rec: RouteGetAppointmentsIdRecord)
}

/** What routeGetAppointmentsId keeps of each BatchGetAppointments response. */
data class RouteGetAppointmentsIdRecord(val correlationId: String, val nextPageToken: String, val message: String)

/** Writes records to acme.driverapp.v1.DriverHomeNotificationService. */
class RouteGetAppointmentsIdDriverHomeNotification(private val stub: DriverHomeNotificationServiceGrpcKt.DriverHomeNotificationServiceCoroutineStub) : RouteGetAppointmentsIdWriter {
    override suspend fun transmit(rec: RouteGetAppointmentsIdRecord) {
        val request = unsubscribeDriverHomeUpdatesRequest {
            this.details = UnsubscribeDriverHomeUpdatesRequestKt.details { this.templateId = rec.message; this.channel = rec.nextPageToken }
        }
        stub.unsubscribeDriverHomeUpdates(request)
    }
}

/** Hands records to acme.maintenance.v1.WorkOrderCalculationService. */
class RouteGetAppointmentsIdWorkOrderCalculation(private val stub: WorkOrderCalculationServiceGrpcKt.WorkOrderCalculationServiceCoroutineStub) : RouteGetAppointmentsIdWriter {
    override suspend fun transmit(rec: RouteGetAppointmentsIdRecord) {
        val request = quoteWorkOrderRequest {
            this.workOrder = workOrder { this.workOrderId = rec.correlationId; this.subject = rec.nextPageToken }
            this.details = QuoteWorkOrderRequestKt.details { this.currencyCode = rec.message }
        }
        stub.quoteWorkOrder(request)
    }
}

/** Writes records to acme.identity.v1.UserQueryService. */
class RouteGetAppointmentsIdUserQuery(private val stub: UserQueryServiceGrpcKt.UserQueryServiceCoroutineStub) : RouteGetAppointmentsIdWriter {
    override suspend fun transmit(rec: RouteGetAppointmentsIdRecord) {
        val request = browseUsersRequest {
            this.page = pageRequest { this.pageToken = rec.nextPageToken }
            this.orderBy = rec.message
        }
        RpcBoundary.run("acme.identity.v1.UserQueryService/BrowseUsers") { stub.browseUsers(request) }
    }
}

/** A sink and the kind of record it takes. */
class RouteGetAppointmentsIdRegistration(val kind: String, val sink: RouteGetAppointmentsIdWriter)
