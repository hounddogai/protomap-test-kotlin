package com.acme.transport.linehaul.trailer.trailermilestone

import com.acme.transport.kit.text.normalize

/** Where a trailer score is in its life cycle. */
enum class TrailerScoreStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One trailer score, with what the planners need to act on it. */
data class TrailerScore(
    var code: String,
    var label: String,
    var unit: String,
    var reviewer: String,
    var version: Int,
    var region: String,
    var archived: Boolean,
    var trailerId: String,
    var status: TrailerScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerScoreStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerScoreStatus): Boolean {
        when (status) {
            TrailerScoreStatus.OPEN -> {
                when (next) {
                    TrailerScoreStatus.IN_REVIEW, TrailerScoreStatus.ARCHIVED -> return true
                }
                return false
            }
            TrailerScoreStatus.IN_REVIEW -> return next == TrailerScoreStatus.REOPENED || next == TrailerScoreStatus.ARCHIVED
            TrailerScoreStatus.RESOLVED -> {
                when (next) {
                    TrailerScoreStatus.REOPENED, TrailerScoreStatus.ARCHIVED -> return true
                }
                return false
            }
            TrailerScoreStatus.REOPENED -> return next == TrailerScoreStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: TrailerScoreStatus) {
        if (!canMoveTo(next)) {
            error("trailer score $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = version.toDouble()

    fun snapshot(): TrailerScore {
        return TrailerScore(
            code = code,
            label = label,
            unit = unit,
            reviewer = reviewer,
            version = version,
            region = region,
            archived = archived,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a trailer score in status open. */
fun newTrailerScore(code: String, label: String): TrailerScore {
    return TrailerScore(
        code = code,
        label = label,
        unit = "",
        reviewer = "",
        version = 0,
        region = "",
        archived = false,
        trailerId = "",
        status = TrailerScoreStatus.OPEN,
    )
}
