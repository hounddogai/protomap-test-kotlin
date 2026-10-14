package com.acme.transport.crewing.roster.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal roster estimate is in its life cycle. */
enum class SeasonalRosterEstimateStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One seasonal roster estimate, with what the planners need to act on it. */
data class SeasonalRosterEstimate(
    var code: String,
    var title: String,
    var durationMinutes: Int,
    var reviewer: String,
    var lane: String,
    var ratio: Double,
    var limit: Int,
    var status: SeasonalRosterEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalRosterEstimateStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalRosterEstimateStatus): Boolean {
        when (status) {
            SeasonalRosterEstimateStatus.REQUESTED -> {
                when (next) {
                    SeasonalRosterEstimateStatus.GRANTED, SeasonalRosterEstimateStatus.EXPIRED -> return true
                }
                return false
            }
            SeasonalRosterEstimateStatus.GRANTED -> return next == SeasonalRosterEstimateStatus.EXPIRED
            SeasonalRosterEstimateStatus.REVOKED -> return next == SeasonalRosterEstimateStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: SeasonalRosterEstimateStatus) {
        if (!canMoveTo(next)) {
            error("seasonal roster estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"seasonal roster estimate"} $code"

    fun effectiveValue(): Double = round((ratio - durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): SeasonalRosterEstimate {
        return SeasonalRosterEstimate(
            code = code,
            title = title,
            durationMinutes = durationMinutes,
            reviewer = reviewer,
            lane = lane,
            ratio = ratio,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a seasonal roster estimate in status requested. */
fun newSeasonalRosterEstimate(code: String, title: String): SeasonalRosterEstimate {
    return SeasonalRosterEstimate(
        code = code,
        title = title,
        durationMinutes = 0,
        reviewer = "",
        lane = "",
        ratio = 0.0,
        limit = 0,
        status = SeasonalRosterEstimateStatus.REQUESTED,
    )
}
