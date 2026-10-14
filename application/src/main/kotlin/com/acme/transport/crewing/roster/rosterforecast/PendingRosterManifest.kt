package com.acme.transport.crewing.roster.rosterforecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending roster manifest is in its life cycle. */
enum class PendingRosterManifestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A pending roster manifest. Its code is unique within a tenant. */
data class PendingRosterManifest(
    var code: String,
    var label: String,
    var ownerId: String,
    var sequence: Int,
    var contactEmail: String,
    var priority: Int,
    var externalRef: String,
    var weight: Double,
    var dock: String,
    var vehicleId: String,
    var retries: Int,
    var status: PendingRosterManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingRosterManifestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingRosterManifestStatus): Boolean {
        when (status) {
            PendingRosterManifestStatus.IDLE -> return next == PendingRosterManifestStatus.READY || next == PendingRosterManifestStatus.DRAINING
            PendingRosterManifestStatus.WARMING -> {
                when (next) {
                    PendingRosterManifestStatus.DRAINING -> return true
                }
                return false
            }
            PendingRosterManifestStatus.READY -> {
                when (next) {
                    PendingRosterManifestStatus.DRAINING -> return true
                }
                return false
            }
            PendingRosterManifestStatus.DRAINING -> {
                when (next) {
                    PendingRosterManifestStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingRosterManifestStatus) {
        if (!canMoveTo(next)) {
            error("pending roster manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"pending roster manifest"} $code"

    fun projectedVolume(): Double = round((sequence.toDouble() + priority.toDouble()) * 100) / 100

    fun snapshot(): PendingRosterManifest {
        return PendingRosterManifest(
            code = code,
            label = label,
            ownerId = ownerId,
            sequence = sequence,
            contactEmail = contactEmail,
            priority = priority,
            externalRef = externalRef,
            weight = weight,
            dock = dock,
            vehicleId = vehicleId,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a pending roster manifest in status idle. */
fun newPendingRosterManifest(code: String, label: String): PendingRosterManifest {
    return PendingRosterManifest(
        code = code,
        label = label,
        ownerId = "",
        sequence = 0,
        contactEmail = "",
        priority = 0,
        externalRef = "",
        weight = 0.0,
        dock = "",
        vehicleId = "",
        retries = 0,
        status = PendingRosterManifestStatus.IDLE,
    )
}
