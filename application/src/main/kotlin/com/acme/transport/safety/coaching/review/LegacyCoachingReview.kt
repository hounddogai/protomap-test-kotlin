package com.acme.transport.safety.coaching.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy coaching review records of safety's coaching work, and the rules, stores, and calculations
// around them.

/** Where a legacy coaching review is in its life cycle. */
enum class LegacyCoachingReviewStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One legacy coaching review, with what the planners need to act on it. */
data class LegacyCoachingReview(
    var code: String,
    var title: String,
    var vehicleId: String,
    var createdAt: Int,
    var limit: Int,
    var durationMinutes: Int,
    var status: LegacyCoachingReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyCoachingReviewStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyCoachingReviewStatus): Boolean {
        when (status) {
            LegacyCoachingReviewStatus.PLANNED -> {
                when (next) {
                    LegacyCoachingReviewStatus.IN_PROGRESS, LegacyCoachingReviewStatus.COMPLETED -> return true
                }
                return false
            }
            LegacyCoachingReviewStatus.SCHEDULED -> return next == LegacyCoachingReviewStatus.IN_PROGRESS || next == LegacyCoachingReviewStatus.COMPLETED
            LegacyCoachingReviewStatus.IN_PROGRESS -> {
                when (next) {
                    LegacyCoachingReviewStatus.COMPLETED -> return true
                }
                return false
            }
            LegacyCoachingReviewStatus.COMPLETED -> return next == LegacyCoachingReviewStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LegacyCoachingReviewStatus) {
        if (!canMoveTo(next)) {
            error("legacy coaching review $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun adjustedExposure(): Double = round(limit.toDouble() * durationMinutes.toDouble() * 100) / 100

    fun snapshot(): LegacyCoachingReview {
        return LegacyCoachingReview(
            code = code,
            title = title,
            vehicleId = vehicleId,
            createdAt = createdAt,
            limit = limit,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a legacy coaching review in status planned. */
fun newLegacyCoachingReview(code: String, title: String): LegacyCoachingReview {
    return LegacyCoachingReview(
        code = code,
        title = title,
        vehicleId = "",
        createdAt = 0,
        limit = 0,
        durationMinutes = 0,
        status = LegacyCoachingReviewStatus.PLANNED,
    )
}
