package com.acme.transport.lastmile.proofofdelivery.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending proof of delivery milestone is in its life cycle. */
enum class PendingProofOfDeliveryMilestoneStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One pending proof of delivery milestone, with what the planners need to act on it. */
data class PendingProofOfDeliveryMilestone(
    var code: String,
    var label: String,
    var etaMinutes: Int,
    var margin: Double,
    var sequence: Int,
    var version: Int,
    var ratio: Double,
    var externalRef: String,
    var status: PendingProofOfDeliveryMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingProofOfDeliveryMilestoneStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingProofOfDeliveryMilestoneStatus): Boolean {
        when (status) {
            PendingProofOfDeliveryMilestoneStatus.PLANNED -> return next == PendingProofOfDeliveryMilestoneStatus.IN_PROGRESS || next == PendingProofOfDeliveryMilestoneStatus.COMPLETED
            PendingProofOfDeliveryMilestoneStatus.SCHEDULED -> return next == PendingProofOfDeliveryMilestoneStatus.IN_PROGRESS || next == PendingProofOfDeliveryMilestoneStatus.CANCELLED
            PendingProofOfDeliveryMilestoneStatus.IN_PROGRESS -> return next == PendingProofOfDeliveryMilestoneStatus.COMPLETED || next == PendingProofOfDeliveryMilestoneStatus.CANCELLED
            PendingProofOfDeliveryMilestoneStatus.COMPLETED -> return next == PendingProofOfDeliveryMilestoneStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: PendingProofOfDeliveryMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("pending proof of delivery milestone $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun netLoad(): Double = round(margin * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): PendingProofOfDeliveryMilestone {
        return PendingProofOfDeliveryMilestone(
            code = code,
            label = label,
            etaMinutes = etaMinutes,
            margin = margin,
            sequence = sequence,
            version = version,
            ratio = ratio,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a pending proof of delivery milestone in status planned. */
fun newPendingProofOfDeliveryMilestone(code: String, label: String): PendingProofOfDeliveryMilestone {
    return PendingProofOfDeliveryMilestone(
        code = code,
        label = label,
        etaMinutes = 0,
        margin = 0.0,
        sequence = 0,
        version = 0,
        ratio = 0.0,
        externalRef = "",
        status = PendingProofOfDeliveryMilestoneStatus.PLANNED,
    )
}
