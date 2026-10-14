package com.acme.transport.safety.coaching.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal coaching estimate is in its life cycle. */
enum class SeasonalCoachingEstimateStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A seasonal coaching estimate. Its code is unique within a tenant. */
data class SeasonalCoachingEstimate(
    var code: String,
    var name: String,
    var durationMinutes: Int,
    var category: String,
    var trailerId: String,
    var endsAt: Int,
    var active: Boolean,
    var batchSize: Int,
    var region: String,
    var status: SeasonalCoachingEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalCoachingEstimateStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalCoachingEstimateStatus): Boolean {
        when (status) {
            SeasonalCoachingEstimateStatus.IDLE -> return next == SeasonalCoachingEstimateStatus.STOPPED
            SeasonalCoachingEstimateStatus.WARMING -> {
                when (next) {
                    SeasonalCoachingEstimateStatus.STOPPED -> return true
                }
                return false
            }
            SeasonalCoachingEstimateStatus.READY -> return next == SeasonalCoachingEstimateStatus.DRAINING || next == SeasonalCoachingEstimateStatus.STOPPED
            SeasonalCoachingEstimateStatus.DRAINING -> {
                when (next) {
                    SeasonalCoachingEstimateStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalCoachingEstimateStatus) {
        if (!canMoveTo(next)) {
            error("seasonal coaching estimate $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun projectedLoad(): Double = round((durationMinutes.toDouble() - batchSize.toDouble()) * 100) / 100

    fun snapshot(): SeasonalCoachingEstimate {
        return SeasonalCoachingEstimate(
            code = code,
            name = name,
            durationMinutes = durationMinutes,
            category = category,
            trailerId = trailerId,
            endsAt = endsAt,
            active = active,
            batchSize = batchSize,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a seasonal coaching estimate in status idle. */
fun newSeasonalCoachingEstimate(code: String, name: String): SeasonalCoachingEstimate {
    return SeasonalCoachingEstimate(
        code = code,
        name = name,
        durationMinutes = 0,
        category = "",
        trailerId = "",
        endsAt = 0,
        active = false,
        batchSize = 0,
        region = "",
        status = SeasonalCoachingEstimateStatus.IDLE,
    )
}
