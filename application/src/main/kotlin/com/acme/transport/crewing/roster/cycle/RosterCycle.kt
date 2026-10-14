package com.acme.transport.crewing.roster.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The roster cycle records of crewing's roster work, and the rules, stores, and calculations around them.

/** Where a roster cycle is in its life cycle. */
enum class RosterCycleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A roster cycle as the roster team keeps it. */
data class RosterCycle(
    var code: String,
    var name: String,
    var startsAt: Int,
    var graceDays: Int,
    var category: String,
    var status: RosterCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterCycleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterCycleStatus): Boolean {
        when (status) {
            RosterCycleStatus.DRAFT -> return next == RosterCycleStatus.ACTIVE
            RosterCycleStatus.PENDING -> return next == RosterCycleStatus.ACTIVE
            RosterCycleStatus.ACTIVE -> return next == RosterCycleStatus.SUSPENDED || next == RosterCycleStatus.CLOSED
            RosterCycleStatus.SUSPENDED -> {
                when (next) {
                    RosterCycleStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RosterCycleStatus) {
        if (!canMoveTo(next)) {
            error("roster cycle $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun netLoad(): Double = round((graceDays.toDouble() + startsAt.toDouble()) * 100) / 100

    fun snapshot(): RosterCycle {
        return RosterCycle(
            code = code,
            name = name,
            startsAt = startsAt,
            graceDays = graceDays,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a roster cycle in status draft. */
fun newRosterCycle(code: String, name: String): RosterCycle {
    return RosterCycle(
        code = code,
        name = name,
        startsAt = 0,
        graceDays = 0,
        category = "",
        status = RosterCycleStatus.DRAFT,
    )
}
