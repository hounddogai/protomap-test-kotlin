package com.acme.transport.emissions.routeoffset.offsetbaseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped route offset baseline records of emissions's route offset work, and the rules, stores, and
// calculations around them.

/** Where a capped route offset baseline is in its life cycle. */
enum class CappedRouteOffsetBaselineStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a capped route offset baseline and its current status. */
data class CappedRouteOffsetBaseline(
    var code: String,
    var name: String,
    var threshold: Double,
    var trailerId: String,
    var maximum: Double,
    var status: CappedRouteOffsetBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedRouteOffsetBaselineStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedRouteOffsetBaselineStatus): Boolean {
        when (status) {
            CappedRouteOffsetBaselineStatus.PROPOSED -> {
                when (next) {
                    CappedRouteOffsetBaselineStatus.WITHDRAWN -> return true
                }
                return false
            }
            CappedRouteOffsetBaselineStatus.APPROVED -> {
                when (next) {
                    CappedRouteOffsetBaselineStatus.REJECTED, CappedRouteOffsetBaselineStatus.WITHDRAWN -> return true
                }
                return false
            }
            CappedRouteOffsetBaselineStatus.REJECTED -> {
                when (next) {
                    CappedRouteOffsetBaselineStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedRouteOffsetBaselineStatus) {
        if (!canMoveTo(next)) {
            error("capped route offset baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun netLoad(): Double = round((maximum - threshold) * 100) / 100

    fun snapshot(): CappedRouteOffsetBaseline {
        return CappedRouteOffsetBaseline(
            code = code,
            name = name,
            threshold = threshold,
            trailerId = trailerId,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a capped route offset baseline in status proposed. */
fun newCappedRouteOffsetBaseline(code: String, name: String): CappedRouteOffsetBaseline {
    return CappedRouteOffsetBaseline(
        code = code,
        name = name,
        threshold = 0.0,
        trailerId = "",
        maximum = 0.0,
        status = CappedRouteOffsetBaselineStatus.PROPOSED,
    )
}
