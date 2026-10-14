package com.acme.transport.crewing.dutyperiod.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive duty period rollup is in its life cycle. */
enum class AdaptiveDutyPeriodRollupStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One adaptive duty period rollup, with what the planners need to act on it. */
data class AdaptiveDutyPeriodRollup(
    var code: String,
    var name: String,
    var endsAt: Int,
    var distanceKm: Double,
    var trailerId: String,
    var threshold: Double,
    var region: String,
    var status: AdaptiveDutyPeriodRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveDutyPeriodRollupStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveDutyPeriodRollupStatus): Boolean {
        when (status) {
            AdaptiveDutyPeriodRollupStatus.IDLE -> {
                when (next) {
                    AdaptiveDutyPeriodRollupStatus.WARMING -> return true
                }
                return false
            }
            AdaptiveDutyPeriodRollupStatus.WARMING -> return next == AdaptiveDutyPeriodRollupStatus.DRAINING || next == AdaptiveDutyPeriodRollupStatus.STOPPED
            AdaptiveDutyPeriodRollupStatus.READY -> return next == AdaptiveDutyPeriodRollupStatus.DRAINING
            AdaptiveDutyPeriodRollupStatus.DRAINING -> {
                when (next) {
                    AdaptiveDutyPeriodRollupStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveDutyPeriodRollupStatus) {
        if (!canMoveTo(next)) {
            error("adaptive duty period rollup $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun projectedLoad(): Double = round((distanceKm + threshold) * 100) / 100

    fun snapshot(): AdaptiveDutyPeriodRollup {
        return AdaptiveDutyPeriodRollup(
            code = code,
            name = name,
            endsAt = endsAt,
            distanceKm = distanceKm,
            trailerId = trailerId,
            threshold = threshold,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a adaptive duty period rollup in status idle. */
fun newAdaptiveDutyPeriodRollup(code: String, name: String): AdaptiveDutyPeriodRollup {
    return AdaptiveDutyPeriodRollup(
        code = code,
        name = name,
        endsAt = 0,
        distanceKm = 0.0,
        trailerId = "",
        threshold = 0.0,
        region = "",
        status = AdaptiveDutyPeriodRollupStatus.IDLE,
    )
}
