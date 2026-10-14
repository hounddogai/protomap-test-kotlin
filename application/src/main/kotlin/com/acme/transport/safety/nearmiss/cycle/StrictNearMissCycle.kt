package com.acme.transport.safety.nearmiss.cycle

import com.acme.transport.kit.text.normalize

// The strict near miss cycle records of safety's near miss work, and the rules, stores, and calculations
// around them.

/** Where a strict near miss cycle is in its life cycle. */
enum class StrictNearMissCycleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a strict near miss cycle and its current status. */
data class StrictNearMissCycle(
    var code: String,
    var title: String,
    var score: Double,
    var currency: String,
    var visible: Boolean,
    var trailerId: String,
    var driverId: String,
    var status: StrictNearMissCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictNearMissCycleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictNearMissCycleStatus): Boolean {
        when (status) {
            StrictNearMissCycleStatus.CREATED -> return next == StrictNearMissCycleStatus.VALIDATED || next == StrictNearMissCycleStatus.RETIRED
            StrictNearMissCycleStatus.VALIDATED -> {
                when (next) {
                    StrictNearMissCycleStatus.PUBLISHED -> return true
                }
                return false
            }
            StrictNearMissCycleStatus.PUBLISHED -> return next == StrictNearMissCycleStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: StrictNearMissCycleStatus) {
        if (!canMoveTo(next)) {
            error("strict near miss cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun measuredValue(): Double = score

    fun snapshot(): StrictNearMissCycle {
        return StrictNearMissCycle(
            code = code,
            title = title,
            score = score,
            currency = currency,
            visible = visible,
            trailerId = trailerId,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a strict near miss cycle in status created. */
fun newStrictNearMissCycle(code: String, title: String): StrictNearMissCycle {
    return StrictNearMissCycle(
        code = code,
        title = title,
        score = 0.0,
        currency = "",
        visible = false,
        trailerId = "",
        driverId = "",
        status = StrictNearMissCycleStatus.CREATED,
    )
}
