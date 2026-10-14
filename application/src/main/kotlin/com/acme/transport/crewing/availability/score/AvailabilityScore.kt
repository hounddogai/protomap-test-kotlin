package com.acme.transport.crewing.availability.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The availability score records of crewing's availability work, and the rules, stores, and calculations
// around them.

/** Where a availability score is in its life cycle. */
enum class AvailabilityScoreStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One availability score, with what the planners need to act on it. */
data class AvailabilityScore(
    var code: String,
    var title: String,
    var retries: Int,
    var lane: String,
    var discount: Double,
    var status: AvailabilityScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AvailabilityScoreStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AvailabilityScoreStatus): Boolean {
        when (status) {
            AvailabilityScoreStatus.PLANNED -> return next == AvailabilityScoreStatus.SCHEDULED || next == AvailabilityScoreStatus.IN_PROGRESS
            AvailabilityScoreStatus.SCHEDULED -> {
                when (next) {
                    AvailabilityScoreStatus.COMPLETED, AvailabilityScoreStatus.CANCELLED -> return true
                }
                return false
            }
            AvailabilityScoreStatus.IN_PROGRESS -> {
                when (next) {
                    AvailabilityScoreStatus.COMPLETED, AvailabilityScoreStatus.CANCELLED -> return true
                }
                return false
            }
            AvailabilityScoreStatus.COMPLETED -> return next == AvailabilityScoreStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: AvailabilityScoreStatus) {
        if (!canMoveTo(next)) {
            error("availability score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun weightedVolume(): Double = round((discount - retries.toDouble()) * 100) / 100

    fun snapshot(): AvailabilityScore {
        return AvailabilityScore(
            code = code,
            title = title,
            retries = retries,
            lane = lane,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a availability score in status planned. */
fun newAvailabilityScore(code: String, title: String): AvailabilityScore {
    return AvailabilityScore(
        code = code,
        title = title,
        retries = 0,
        lane = "",
        discount = 0.0,
        status = AvailabilityScoreStatus.PLANNED,
    )
}
