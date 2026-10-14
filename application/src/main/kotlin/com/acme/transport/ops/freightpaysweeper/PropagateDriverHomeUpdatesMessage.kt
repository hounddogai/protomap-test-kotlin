package com.acme.transport.ops.freightpaysweeper

import com.acme.admissions.v1.AdmissionServiceGrpcKt
import com.acme.admissions.v1.deleteAdmissionRequest
import com.acme.customers.v1.PreferenceSyncServiceGrpcKt
import com.acme.customers.v1.resolvePreferenceConflictRequest
import com.acme.driverapp.v1.DriverHomeNotificationServiceGrpcKt
import com.acme.driverapp.v1.subscribeDriverHomeUpdatesRequest
import com.acme.subscriptions.v1.SubscriptionServiceGrpcKt
import com.acme.subscriptions.v1.createSubscriptionRequest
import com.acme.subscriptions.v1.subscription
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.driverapp.v1.DriverHomeNotificationService/SubscribeDriverHomeUpdates and hands what it keeps to the sink of its kind. */
suspend fun propagateDriverHomeUpdatesMessage(channel: ManagedChannel) {
    val reader = DriverHomeNotificationServiceGrpcKt.DriverHomeNotificationServiceCoroutineStub(channel)
    val got = RpcBoundary.read { reader.subscribeDriverHomeUpdates(subscribeDriverHomeUpdatesRequest { this.reason = "current-propagate-driver-home-updates-message" }) }
    val entry = PropagateDriverHomeUpdatesMessageRecord(
        message = normalize(got.result.message),
        driverHomeId = got.driverHome.driverHomeId,
        subject = got.driverHome.subject,
        correlationId = got.result.correlationId,
    )
    actOnPropagateDriverHomeUpdatesMessage("preference-sync", entry, channel)
}

/** Where propagateDriverHomeUpdatesMessage hands each record. */
interface PropagateDriverHomeUpdatesMessageDestination {
    suspend fun transmit(rec: PropagateDriverHomeUpdatesMessageRecord)
}

/** What propagateDriverHomeUpdatesMessage keeps of each SubscribeDriverHomeUpdates response. */
data class PropagateDriverHomeUpdatesMessageRecord(val message: String, val driverHomeId: String, val subject: String, val correlationId: String)

/** Hands records to acme.admissions.v1.AdmissionService. */
class PropagateDriverHomeUpdatesMessageAdmission(private val stub: AdmissionServiceGrpcKt.AdmissionServiceCoroutineStub) : PropagateDriverHomeUpdatesMessageDestination {
    override suspend fun transmit(rec: PropagateDriverHomeUpdatesMessageRecord) {
        val request = deleteAdmissionRequest {
            this.admissionId = rec.correlationId
            this.etag = rec.driverHomeId
            this.reason = rec.subject
        }
        stub.deleteAdmission(request)
    }
}

/** Passes records to acme.subscriptions.v1.SubscriptionService. */
class PropagateDriverHomeUpdatesMessageSubscription(private val stub: SubscriptionServiceGrpcKt.SubscriptionServiceCoroutineStub) : PropagateDriverHomeUpdatesMessageDestination {
    override suspend fun transmit(rec: PropagateDriverHomeUpdatesMessageRecord) {
        val request = createSubscriptionRequest {
            this.requestId = rec.driverHomeId
            this.subscription = subscription { this.currencyCode = rec.message }
        }
        stub.createSubscription(request)
    }
}

/** Passes records to acme.customers.v1.PreferenceSyncService. */
class PropagateDriverHomeUpdatesMessagePreferenceSync(private val stub: PreferenceSyncServiceGrpcKt.PreferenceSyncServiceCoroutineStub) : PropagateDriverHomeUpdatesMessageDestination {
    override suspend fun transmit(rec: PropagateDriverHomeUpdatesMessageRecord) {
        stub.resolvePreferenceConflict(resolvePreferenceConflictRequest { this.preferenceId = rec.driverHomeId })
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnPropagateDriverHomeUpdatesMessage(kind: String, rec: PropagateDriverHomeUpdatesMessageRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "admission" to { PropagateDriverHomeUpdatesMessageAdmission(AdmissionServiceGrpcKt.AdmissionServiceCoroutineStub(channel)).transmit(rec) },
        "subscription" to { PropagateDriverHomeUpdatesMessageSubscription(SubscriptionServiceGrpcKt.SubscriptionServiceCoroutineStub(channel)).transmit(rec) },
        "preference-sync" to { PropagateDriverHomeUpdatesMessagePreferenceSync(PreferenceSyncServiceGrpcKt.PreferenceSyncServiceCoroutineStub(channel)).transmit(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
