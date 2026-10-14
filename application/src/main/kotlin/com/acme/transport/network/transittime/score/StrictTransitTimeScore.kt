package com.acme.transport.network.transittime.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict transit time score records of network's transit time work, and the rules, stores, and
// calculations around them.

/** Where a strict transit time score is in its life cycle. */
enum class StrictTransitTimeScoreStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A strict transit time score as the transit time team keeps it. */
data class StrictTransitTimeScore(
    var code: String,
    var name: String,
    var retries: Int,
    var ceiling: Double,
    var carrier: String,
    var routeCode: String,
    var floorValue: Double,
    var depot: String,
    var sequence: Int,
    var maximum: Double,
    var status: StrictTransitTimeScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictTransitTimeScoreStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictTransitTimeScoreStatus): Boolean {
        when (status) {
            StrictTransitTimeScoreStatus.PLANNED -> {
                when (next) {
                    StrictTransitTimeScoreStatus.COMPLETED -> return true
                }
                return false
            }
            StrictTransitTimeScoreStatus.SCHEDULED -> {
                when (next) {
                    StrictTransitTimeScoreStatus.COMPLETED -> return true
                }
                return false
            }
            StrictTransitTimeScoreStatus.IN_PROGRESS -> {
                when (next) {
                    StrictTransitTimeScoreStatus.COMPLETED -> return true
                }
                return false
            }
            StrictTransitTimeScoreStatus.COMPLETED -> return next == StrictTransitTimeScoreStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: StrictTransitTimeScoreStatus) {
        if (!canMoveTo(next)) {
            error("strict transit time score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"strict transit time score"} $code"

    fun netExposure(): Double = round(maximum * floorValue * 100) / 100

    fun snapshot(): StrictTransitTimeScore {
        return StrictTransitTimeScore(
            code = code,
            name = name,
            retries = retries,
            ceiling = ceiling,
            carrier = carrier,
            routeCode = routeCode,
            floorValue = floorValue,
            depot = depot,
            sequence = sequence,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a strict transit time score in status planned. */
fun newStrictTransitTimeScore(code: String, name: String): StrictTransitTimeScore {
    return StrictTransitTimeScore(
        code = code,
        name = name,
        retries = 0,
        ceiling = 0.0,
        carrier = "",
        routeCode = "",
        floorValue = 0.0,
        depot = "",
        sequence = 0,
        maximum = 0.0,
        status = StrictTransitTimeScoreStatus.PLANNED,
    )
}
