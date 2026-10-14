package com.acme.transport.lastmile.proofofdelivery.entry

import com.acme.transport.kit.text.normalize

/** Where a seasonal proof of delivery plan is in its life cycle. */
enum class SeasonalProofOfDeliveryPlanStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A seasonal proof of delivery plan as the proof of delivery team keeps it. */
data class SeasonalProofOfDeliveryPlan(
    var code: String,
    var name: String,
    var maximum: Double,
    var checksum: String,
    var locale: String,
    var status: SeasonalProofOfDeliveryPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalProofOfDeliveryPlanStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalProofOfDeliveryPlanStatus): Boolean {
        when (status) {
            SeasonalProofOfDeliveryPlanStatus.IDLE -> return next == SeasonalProofOfDeliveryPlanStatus.READY || next == SeasonalProofOfDeliveryPlanStatus.STOPPED
            SeasonalProofOfDeliveryPlanStatus.WARMING -> return next == SeasonalProofOfDeliveryPlanStatus.READY
            SeasonalProofOfDeliveryPlanStatus.READY -> return next == SeasonalProofOfDeliveryPlanStatus.DRAINING || next == SeasonalProofOfDeliveryPlanStatus.STOPPED
            SeasonalProofOfDeliveryPlanStatus.DRAINING -> {
                when (next) {
                    SeasonalProofOfDeliveryPlanStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalProofOfDeliveryPlanStatus) {
        if (!canMoveTo(next)) {
            error("seasonal proof of delivery plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = maximum

    fun snapshot(): SeasonalProofOfDeliveryPlan {
        return SeasonalProofOfDeliveryPlan(
            code = code,
            name = name,
            maximum = maximum,
            checksum = checksum,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a seasonal proof of delivery plan in status idle. */
fun newSeasonalProofOfDeliveryPlan(code: String, name: String): SeasonalProofOfDeliveryPlan {
    return SeasonalProofOfDeliveryPlan(
        code = code,
        name = name,
        maximum = 0.0,
        checksum = "",
        locale = "",
        status = SeasonalProofOfDeliveryPlanStatus.IDLE,
    )
}
