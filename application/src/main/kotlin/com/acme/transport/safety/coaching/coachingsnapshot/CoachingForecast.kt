package com.acme.transport.safety.coaching.coachingsnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a coaching forecast is in its life cycle. */
enum class CoachingForecastStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A coaching forecast. Its code is unique within a tenant. */
data class CoachingForecast(
    var code: String,
    var title: String,
    var trailerId: String,
    var quantity: Int,
    var carrier: String,
    var distanceKm: Double,
    var status: CoachingForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingForecastStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingForecastStatus): Boolean {
        when (status) {
            CoachingForecastStatus.PROPOSED -> {
                when (next) {
                    CoachingForecastStatus.APPROVED -> return true
                }
                return false
            }
            CoachingForecastStatus.APPROVED -> return next == CoachingForecastStatus.REJECTED || next == CoachingForecastStatus.WITHDRAWN
            CoachingForecastStatus.REJECTED -> return next == CoachingForecastStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: CoachingForecastStatus) {
        if (!canMoveTo(next)) {
            error("coaching forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"coaching forecast"} $code"

    fun projectedValue(): Double = round(distanceKm * quantity.toDouble() * 100) / 100

    fun snapshot(): CoachingForecast {
        return CoachingForecast(
            code = code,
            title = title,
            trailerId = trailerId,
            quantity = quantity,
            carrier = carrier,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a coaching forecast in status proposed. */
fun newCoachingForecast(code: String, title: String): CoachingForecast {
    return CoachingForecast(
        code = code,
        title = title,
        trailerId = "",
        quantity = 0,
        carrier = "",
        distanceKm = 0.0,
        status = CoachingForecastStatus.PROPOSED,
    )
}
