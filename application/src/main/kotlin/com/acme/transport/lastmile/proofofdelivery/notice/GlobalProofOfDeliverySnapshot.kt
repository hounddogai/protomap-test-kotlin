package com.acme.transport.lastmile.proofofdelivery.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global proof of delivery snapshot is in its life cycle. */
enum class GlobalProofOfDeliverySnapshotStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a global proof of delivery snapshot and its current status. */
data class GlobalProofOfDeliverySnapshot(
    var code: String,
    var label: String,
    var loadKg: Double,
    var durationMinutes: Int,
    var cost: Double,
    var windowDays: Int,
    var carrier: String,
    var status: GlobalProofOfDeliverySnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalProofOfDeliverySnapshotStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalProofOfDeliverySnapshotStatus): Boolean {
        when (status) {
            GlobalProofOfDeliverySnapshotStatus.PLANNED -> return next == GlobalProofOfDeliverySnapshotStatus.SCHEDULED || next == GlobalProofOfDeliverySnapshotStatus.IN_PROGRESS
            GlobalProofOfDeliverySnapshotStatus.SCHEDULED -> {
                when (next) {
                    GlobalProofOfDeliverySnapshotStatus.COMPLETED -> return true
                }
                return false
            }
            GlobalProofOfDeliverySnapshotStatus.IN_PROGRESS -> {
                when (next) {
                    GlobalProofOfDeliverySnapshotStatus.CANCELLED -> return true
                }
                return false
            }
            GlobalProofOfDeliverySnapshotStatus.COMPLETED -> {
                when (next) {
                    GlobalProofOfDeliverySnapshotStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalProofOfDeliverySnapshotStatus) {
        if (!canMoveTo(next)) {
            error("global proof of delivery snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun netVolume(): Double = round((durationMinutes.toDouble() - cost) * 100) / 100

    fun snapshot(): GlobalProofOfDeliverySnapshot {
        return GlobalProofOfDeliverySnapshot(
            code = code,
            label = label,
            loadKg = loadKg,
            durationMinutes = durationMinutes,
            cost = cost,
            windowDays = windowDays,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a global proof of delivery snapshot in status planned. */
fun newGlobalProofOfDeliverySnapshot(code: String, label: String): GlobalProofOfDeliverySnapshot {
    return GlobalProofOfDeliverySnapshot(
        code = code,
        label = label,
        loadKg = 0.0,
        durationMinutes = 0,
        cost = 0.0,
        windowDays = 0,
        carrier = "",
        status = GlobalProofOfDeliverySnapshotStatus.PLANNED,
    )
}
