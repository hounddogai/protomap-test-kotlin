package com.acme.transport.lastmile.proofofdelivery.deliveryscore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal proof of delivery milestone is in its life cycle. */
enum class SeasonalProofOfDeliveryMilestoneStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A seasonal proof of delivery milestone. Its code is unique within a tenant. */
data class SeasonalProofOfDeliveryMilestone(
    var code: String,
    var name: String,
    var maximum: Double,
    var priority: Int,
    var minimum: Double,
    var ratio: Double,
    var pinned: Boolean,
    var rate: Double,
    var odometer: Int,
    var status: SeasonalProofOfDeliveryMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalProofOfDeliveryMilestoneStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalProofOfDeliveryMilestoneStatus): Boolean {
        when (status) {
            SeasonalProofOfDeliveryMilestoneStatus.NEW -> return next == SeasonalProofOfDeliveryMilestoneStatus.QUEUED || next == SeasonalProofOfDeliveryMilestoneStatus.FAILED
            SeasonalProofOfDeliveryMilestoneStatus.QUEUED -> return next == SeasonalProofOfDeliveryMilestoneStatus.RUNNING || next == SeasonalProofOfDeliveryMilestoneStatus.DONE
            SeasonalProofOfDeliveryMilestoneStatus.RUNNING -> return next == SeasonalProofOfDeliveryMilestoneStatus.DONE || next == SeasonalProofOfDeliveryMilestoneStatus.FAILED
            SeasonalProofOfDeliveryMilestoneStatus.DONE -> return next == SeasonalProofOfDeliveryMilestoneStatus.FAILED
        }
        return false
    }

    fun moveTo(next: SeasonalProofOfDeliveryMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("seasonal proof of delivery milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"seasonal proof of delivery milestone"} $code"

    fun weightedTotal(): Double = round((odometer.toDouble() - rate) * 100) / 100

    fun snapshot(): SeasonalProofOfDeliveryMilestone {
        return SeasonalProofOfDeliveryMilestone(
            code = code,
            name = name,
            maximum = maximum,
            priority = priority,
            minimum = minimum,
            ratio = ratio,
            pinned = pinned,
            rate = rate,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a seasonal proof of delivery milestone in status new. */
fun newSeasonalProofOfDeliveryMilestone(code: String, name: String): SeasonalProofOfDeliveryMilestone {
    return SeasonalProofOfDeliveryMilestone(
        code = code,
        name = name,
        maximum = 0.0,
        priority = 0,
        minimum = 0.0,
        ratio = 0.0,
        pinned = false,
        rate = 0.0,
        odometer = 0,
        status = SeasonalProofOfDeliveryMilestoneStatus.NEW,
    )
}
