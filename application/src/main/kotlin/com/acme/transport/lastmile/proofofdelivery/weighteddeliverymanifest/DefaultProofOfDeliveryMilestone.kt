package com.acme.transport.lastmile.proofofdelivery.weighteddeliverymanifest

import com.acme.transport.kit.text.normalize

/** Where a default proof of delivery milestone is in its life cycle. */
enum class DefaultProofOfDeliveryMilestoneStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A default proof of delivery milestone as the proof of delivery team keeps it. */
data class DefaultProofOfDeliveryMilestone(
    var code: String,
    var name: String,
    var attempts: Int,
    var driverId: String,
    var segment: String,
    var reviewer: String,
    var status: DefaultProofOfDeliveryMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultProofOfDeliveryMilestoneStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultProofOfDeliveryMilestoneStatus): Boolean {
        when (status) {
            DefaultProofOfDeliveryMilestoneStatus.IDLE -> return next == DefaultProofOfDeliveryMilestoneStatus.READY
            DefaultProofOfDeliveryMilestoneStatus.WARMING -> return next == DefaultProofOfDeliveryMilestoneStatus.READY || next == DefaultProofOfDeliveryMilestoneStatus.DRAINING
            DefaultProofOfDeliveryMilestoneStatus.READY -> return next == DefaultProofOfDeliveryMilestoneStatus.STOPPED
            DefaultProofOfDeliveryMilestoneStatus.DRAINING -> {
                when (next) {
                    DefaultProofOfDeliveryMilestoneStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultProofOfDeliveryMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("default proof of delivery milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = attempts.toDouble()

    fun snapshot(): DefaultProofOfDeliveryMilestone {
        return DefaultProofOfDeliveryMilestone(
            code = code,
            name = name,
            attempts = attempts,
            driverId = driverId,
            segment = segment,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a default proof of delivery milestone in status idle. */
fun newDefaultProofOfDeliveryMilestone(code: String, name: String): DefaultProofOfDeliveryMilestone {
    return DefaultProofOfDeliveryMilestone(
        code = code,
        name = name,
        attempts = 0,
        driverId = "",
        segment = "",
        reviewer = "",
        status = DefaultProofOfDeliveryMilestoneStatus.IDLE,
    )
}
