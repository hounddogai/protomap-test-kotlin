package com.acme.transport.safety.coaching.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The coaching estimate records of safety's coaching work, and the rules, stores, and calculations around
// them.

/** Where a coaching estimate is in its life cycle. */
enum class CoachingEstimateStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One coaching estimate, with what the planners need to act on it. */
data class CoachingEstimate(
    var code: String,
    var title: String,
    var margin: Double,
    var windowDays: Int,
    var amount: Double,
    var notes: String,
    var endsAt: Int,
    var status: CoachingEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingEstimateStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingEstimateStatus): Boolean {
        when (status) {
            CoachingEstimateStatus.OPEN -> {
                when (next) {
                    CoachingEstimateStatus.IN_REVIEW -> return true
                }
                return false
            }
            CoachingEstimateStatus.IN_REVIEW -> {
                when (next) {
                    CoachingEstimateStatus.RESOLVED, CoachingEstimateStatus.ARCHIVED -> return true
                }
                return false
            }
            CoachingEstimateStatus.RESOLVED -> {
                when (next) {
                    CoachingEstimateStatus.ARCHIVED -> return true
                }
                return false
            }
            CoachingEstimateStatus.REOPENED -> {
                when (next) {
                    CoachingEstimateStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CoachingEstimateStatus) {
        if (!canMoveTo(next)) {
            error("coaching estimate $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun weightedValue(): Double = round((amount - endsAt.toDouble()) * 100) / 100

    fun snapshot(): CoachingEstimate {
        return CoachingEstimate(
            code = code,
            title = title,
            margin = margin,
            windowDays = windowDays,
            amount = amount,
            notes = notes,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a coaching estimate in status open. */
fun newCoachingEstimate(code: String, title: String): CoachingEstimate {
    return CoachingEstimate(
        code = code,
        title = title,
        margin = 0.0,
        windowDays = 0,
        amount = 0.0,
        notes = "",
        endsAt = 0,
        status = CoachingEstimateStatus.OPEN,
    )
}
