package com.acme.transport.terminal.crossdock.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient cross dock baseline records of terminal's cross dock work, and the rules, stores, and
// calculations around them.

/** Where a lenient cross dock baseline is in its life cycle. */
enum class LenientCrossDockBaselineStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One lenient cross dock baseline, with what the planners need to act on it. */
data class LenientCrossDockBaseline(
    var code: String,
    var name: String,
    var budget: Double,
    var margin: Double,
    var lane: String,
    var sequence: Int,
    var phone: String,
    var depot: String,
    var trailerId: String,
    var status: LenientCrossDockBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientCrossDockBaselineStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientCrossDockBaselineStatus): Boolean {
        when (status) {
            LenientCrossDockBaselineStatus.OPEN -> {
                when (next) {
                    LenientCrossDockBaselineStatus.ARCHIVED -> return true
                }
                return false
            }
            LenientCrossDockBaselineStatus.IN_REVIEW -> return next == LenientCrossDockBaselineStatus.RESOLVED
            LenientCrossDockBaselineStatus.RESOLVED -> return next == LenientCrossDockBaselineStatus.ARCHIVED
            LenientCrossDockBaselineStatus.REOPENED -> return next == LenientCrossDockBaselineStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: LenientCrossDockBaselineStatus) {
        if (!canMoveTo(next)) {
            error("lenient cross dock baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"lenient cross dock baseline"} $code"

    fun netLoad(): Double = round((sequence.toDouble() - margin) * 100) / 100

    fun snapshot(): LenientCrossDockBaseline {
        return LenientCrossDockBaseline(
            code = code,
            name = name,
            budget = budget,
            margin = margin,
            lane = lane,
            sequence = sequence,
            phone = phone,
            depot = depot,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a lenient cross dock baseline in status open. */
fun newLenientCrossDockBaseline(code: String, name: String): LenientCrossDockBaseline {
    return LenientCrossDockBaseline(
        code = code,
        name = name,
        budget = 0.0,
        margin = 0.0,
        lane = "",
        sequence = 0,
        phone = "",
        depot = "",
        trailerId = "",
        status = LenientCrossDockBaselineStatus.OPEN,
    )
}
