package com.acme.transport.crewing.restbreak.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted rest break score is in its life cycle. */
enum class WeightedRestBreakScoreStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a weighted rest break score and its current status. */
data class WeightedRestBreakScore(
    var code: String,
    var title: String,
    var summary: String,
    var approved: Boolean,
    var updatedAt: Int,
    var score: Double,
    var status: WeightedRestBreakScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedRestBreakScoreStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedRestBreakScoreStatus): Boolean {
        when (status) {
            WeightedRestBreakScoreStatus.PLANNED -> return next == WeightedRestBreakScoreStatus.SCHEDULED || next == WeightedRestBreakScoreStatus.IN_PROGRESS
            WeightedRestBreakScoreStatus.SCHEDULED -> return next == WeightedRestBreakScoreStatus.CANCELLED
            WeightedRestBreakScoreStatus.IN_PROGRESS -> return next == WeightedRestBreakScoreStatus.CANCELLED
            WeightedRestBreakScoreStatus.COMPLETED -> {
                when (next) {
                    WeightedRestBreakScoreStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedRestBreakScoreStatus) {
        if (!canMoveTo(next)) {
            error("weighted rest break score $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun effectiveExposure(): Double = round((score + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): WeightedRestBreakScore {
        return WeightedRestBreakScore(
            code = code,
            title = title,
            summary = summary,
            approved = approved,
            updatedAt = updatedAt,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a weighted rest break score in status planned. */
fun newWeightedRestBreakScore(code: String, title: String): WeightedRestBreakScore {
    return WeightedRestBreakScore(
        code = code,
        title = title,
        summary = "",
        approved = false,
        updatedAt = 0,
        score = 0.0,
        status = WeightedRestBreakScoreStatus.PLANNED,
    )
}
