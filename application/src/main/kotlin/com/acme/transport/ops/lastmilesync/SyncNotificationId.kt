package com.acme.transport.ops.lastmilesync

import com.acme.customers.v1.ConsentRecordSyncServiceGrpcKt
import com.acme.customers.v1.consentRecord
import com.acme.customers.v1.pullConsentRecordsRequest
import com.acme.fleet.v1.DelegateVehicleGroupApprovalRequestKt
import com.acme.fleet.v1.VehicleGroupApprovalServiceGrpcKt
import com.acme.fleet.v1.delegateVehicleGroupApprovalRequest
import com.acme.notifications.v1.NotificationServiceGrpcKt
import com.acme.notifications.v1.cloneNotificationRequest
import com.acme.routing.v1.RoutePlanApprovalServiceGrpcKt
import com.acme.routing.v1.rejectRoutePlanRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.notifications.v1.NotificationService/CloneNotification and hands what it keeps to the sink of its kind. */
suspend fun syncNotificationId(channel: ManagedChannel) {
    val source = NotificationServiceGrpcKt.NotificationServiceCoroutineStub(channel)
    val current = source.cloneNotification(cloneNotificationRequest { this.requestId = "main-sync-notification-id" })
    val entry = SyncNotificationIdRecord(
        notificationId = current.notification.notificationId,
        status = current.result.status,
    )
    val sinks = mapOf<String, SyncNotificationIdTarget>(
        "route-plan-approval" to SyncNotificationIdRoutePlanApproval(RoutePlanApprovalServiceGrpcKt.RoutePlanApprovalServiceCoroutineStub(channel)),
        "consent-record-sync" to SyncNotificationIdConsentRecordSync(ConsentRecordSyncServiceGrpcKt.ConsentRecordSyncServiceCoroutineStub(channel)),
        "vehicle-group-approval" to SyncNotificationIdVehicleGroupApproval(VehicleGroupApprovalServiceGrpcKt.VehicleGroupApprovalServiceCoroutineStub(channel)),
    )
    val sink = sinks[System.getenv("SYNC_NOTIFICATION_ID_TARGET")] ?: return
    sink.relay(entry)
}

/** Where syncNotificationId hands each record. */
interface SyncNotificationIdTarget {
    suspend fun relay(rec: SyncNotificationIdRecord)
}

/** What syncNotificationId keeps of each CloneNotification response. */
data class SyncNotificationIdRecord(val notificationId: String, val status: String)

/** Writes records to acme.routing.v1.RoutePlanApprovalService. */
class SyncNotificationIdRoutePlanApproval(private val stub: RoutePlanApprovalServiceGrpcKt.RoutePlanApprovalServiceCoroutineStub) : SyncNotificationIdTarget {
    override suspend fun relay(rec: SyncNotificationIdRecord) {
        stub.rejectRoutePlan(rejectRoutePlanRequest { this.reason = rec.notificationId })
    }
}

/** Sends records to acme.customers.v1.ConsentRecordSyncService. */
class SyncNotificationIdConsentRecordSync(private val stub: ConsentRecordSyncServiceGrpcKt.ConsentRecordSyncServiceCoroutineStub) : SyncNotificationIdTarget {
    override suspend fun relay(rec: SyncNotificationIdRecord) {
        val request = pullConsentRecordsRequest {
            this.consentRecord = consentRecord { this.source = rec.notificationId }
        }
        stub.pullConsentRecords(request)
    }
}

/** Writes records to acme.fleet.v1.VehicleGroupApprovalService. */
class SyncNotificationIdVehicleGroupApproval(private val stub: VehicleGroupApprovalServiceGrpcKt.VehicleGroupApprovalServiceCoroutineStub) : SyncNotificationIdTarget {
    override suspend fun relay(rec: SyncNotificationIdRecord) {
        val request = delegateVehicleGroupApprovalRequest {
            this.vehicleGroupId = rec.status
            this.details = DelegateVehicleGroupApprovalRequestKt.details { this.approverEmail = rec.notificationId }
        }
        RpcBoundary.run("acme.fleet.v1.VehicleGroupApprovalService/DelegateVehicleGroupApproval") { stub.delegateVehicleGroupApproval(request) }
    }
}
