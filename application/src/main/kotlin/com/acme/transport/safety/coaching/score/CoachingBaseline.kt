package com.acme.transport.safety.coaching.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a coaching baseline is in its life cycle. */
enum class CoachingBaselineStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One coaching baseline, with what the planners need to act on it. */
data class CoachingBaseline(
    var code: String,
    var label: String,
    var unit: String,
    var margin: Double,
    var endsAt: Int,
    var score: Double,
    var currency: String,
    var reviewer: String,
    var limit: Int,
    var country: String,
    var odometer: Int,
    var status: CoachingBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingBaselineStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingBaselineStatus): Boolean {
        when (status) {
            CoachingBaselineStatus.DRAFT -> return next == CoachingBaselineStatus.CLOSED
            CoachingBaselineStatus.PENDING -> {
                when (next) {
                    CoachingBaselineStatus.ACTIVE -> return true
                }
                return false
            }
            CoachingBaselineStatus.ACTIVE -> return next == CoachingBaselineStatus.CLOSED
            CoachingBaselineStatus.SUSPENDED -> return next == CoachingBaselineStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: CoachingBaselineStatus) {
        if (!canMoveTo(next)) {
            error("coaching baseline $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"coaching baseline"} $code"

    fun projectedValue(): Double = round(endsAt.toDouble() * odometer.toDouble() * 100) / 100

    fun snapshot(): CoachingBaseline {
        return CoachingBaseline(
            code = code,
            label = label,
            unit = unit,
            margin = margin,
            endsAt = endsAt,
            score = score,
            currency = currency,
            reviewer = reviewer,
            limit = limit,
            country = country,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a coaching baseline in status draft. */
fun newCoachingBaseline(code: String, label: String): CoachingBaseline {
    return CoachingBaseline(
        code = code,
        label = label,
        unit = "",
        margin = 0.0,
        endsAt = 0,
        score = 0.0,
        currency = "",
        reviewer = "",
        limit = 0,
        country = "",
        odometer = 0,
        status = CoachingBaselineStatus.DRAFT,
    )
}
