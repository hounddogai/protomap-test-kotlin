package com.acme.transport.fleetops.utilization.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict utilization review is in its life cycle. */
enum class StrictUtilizationReviewStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One strict utilization review, with what the planners need to act on it. */
data class StrictUtilizationReview(
    var code: String,
    var title: String,
    var segment: String,
    var createdAt: Int,
    var floorValue: Double,
    var timezone: String,
    var rate: Double,
    var status: StrictUtilizationReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictUtilizationReviewStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictUtilizationReviewStatus): Boolean {
        when (status) {
            StrictUtilizationReviewStatus.PLANNED -> {
                when (next) {
                    StrictUtilizationReviewStatus.SCHEDULED, StrictUtilizationReviewStatus.CANCELLED -> return true
                }
                return false
            }
            StrictUtilizationReviewStatus.SCHEDULED -> return next == StrictUtilizationReviewStatus.IN_PROGRESS || next == StrictUtilizationReviewStatus.CANCELLED
            StrictUtilizationReviewStatus.IN_PROGRESS -> {
                when (next) {
                    StrictUtilizationReviewStatus.CANCELLED -> return true
                }
                return false
            }
            StrictUtilizationReviewStatus.COMPLETED -> {
                when (next) {
                    StrictUtilizationReviewStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictUtilizationReviewStatus) {
        if (!canMoveTo(next)) {
            error("strict utilization review $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun weightedValue(): Double = round(floorValue * createdAt.toDouble() * 100) / 100

    fun snapshot(): StrictUtilizationReview {
        return StrictUtilizationReview(
            code = code,
            title = title,
            segment = segment,
            createdAt = createdAt,
            floorValue = floorValue,
            timezone = timezone,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a strict utilization review in status planned. */
fun newStrictUtilizationReview(code: String, title: String): StrictUtilizationReview {
    return StrictUtilizationReview(
        code = code,
        title = title,
        segment = "",
        createdAt = 0,
        floorValue = 0.0,
        timezone = "",
        rate = 0.0,
        status = StrictUtilizationReviewStatus.PLANNED,
    )
}
