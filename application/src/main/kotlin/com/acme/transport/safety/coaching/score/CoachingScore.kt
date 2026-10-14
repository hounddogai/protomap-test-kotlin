package com.acme.transport.safety.coaching.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The coaching score records of safety's coaching work, and the rules, stores, and calculations around them.

/** Where a coaching score is in its life cycle. */
enum class CoachingScoreStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A coaching score as the coaching team keeps it. */
data class CoachingScore(
    var code: String,
    var label: String,
    var country: String,
    var segment: String,
    var margin: Double,
    var locale: String,
    var lane: String,
    var expiresAt: Int,
    var phone: String,
    var depot: String,
    var status: CoachingScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingScoreStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingScoreStatus): Boolean {
        when (status) {
            CoachingScoreStatus.PLANNED -> {
                when (next) {
                    CoachingScoreStatus.SCHEDULED, CoachingScoreStatus.CANCELLED -> return true
                }
                return false
            }
            CoachingScoreStatus.SCHEDULED -> {
                when (next) {
                    CoachingScoreStatus.CANCELLED -> return true
                }
                return false
            }
            CoachingScoreStatus.IN_PROGRESS -> return next == CoachingScoreStatus.CANCELLED
            CoachingScoreStatus.COMPLETED -> {
                when (next) {
                    CoachingScoreStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CoachingScoreStatus) {
        if (!canMoveTo(next)) {
            error("coaching score $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"coaching score"} $code"

    fun effectiveLoad(): Double = round((margin - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): CoachingScore {
        return CoachingScore(
            code = code,
            label = label,
            country = country,
            segment = segment,
            margin = margin,
            locale = locale,
            lane = lane,
            expiresAt = expiresAt,
            phone = phone,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a coaching score in status planned. */
fun newCoachingScore(code: String, label: String): CoachingScore {
    return CoachingScore(
        code = code,
        label = label,
        country = "",
        segment = "",
        margin = 0.0,
        locale = "",
        lane = "",
        expiresAt = 0,
        phone = "",
        depot = "",
        status = CoachingScoreStatus.PLANNED,
    )
}
