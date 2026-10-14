package com.acme.transport.crewing.roster.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a roster target is in its life cycle. */
enum class RosterTargetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One roster target, with what the planners need to act on it. */
data class RosterTarget(
    var code: String,
    var name: String,
    var priority: Int,
    var ownerId: String,
    var amount: Double,
    var status: RosterTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterTargetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterTargetStatus): Boolean {
        when (status) {
            RosterTargetStatus.CREATED -> return next == RosterTargetStatus.PUBLISHED
            RosterTargetStatus.VALIDATED -> return next == RosterTargetStatus.PUBLISHED || next == RosterTargetStatus.RETIRED
            RosterTargetStatus.PUBLISHED -> {
                when (next) {
                    RosterTargetStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RosterTargetStatus) {
        if (!canMoveTo(next)) {
            error("roster target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"roster target"} $code"

    fun weightedExposure(): Double = round(priority.toDouble() * amount * 100) / 100

    fun snapshot(): RosterTarget {
        return RosterTarget(
            code = code,
            name = name,
            priority = priority,
            ownerId = ownerId,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a roster target in status created. */
fun newRosterTarget(code: String, name: String): RosterTarget {
    return RosterTarget(
        code = code,
        name = name,
        priority = 0,
        ownerId = "",
        amount = 0.0,
        status = RosterTargetStatus.CREATED,
    )
}
