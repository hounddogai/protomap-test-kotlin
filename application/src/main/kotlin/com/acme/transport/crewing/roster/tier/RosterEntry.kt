package com.acme.transport.crewing.roster.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a roster entry is in its life cycle. */
enum class RosterEntryStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A roster entry as the roster team keeps it. */
data class RosterEntry(
    var code: String,
    var label: String,
    var retries: Int,
    var description: String,
    var startsAt: Int,
    var depot: String,
    var status: RosterEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterEntryStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterEntryStatus): Boolean {
        when (status) {
            RosterEntryStatus.IDLE -> return next == RosterEntryStatus.DRAINING
            RosterEntryStatus.WARMING -> return next == RosterEntryStatus.READY || next == RosterEntryStatus.DRAINING
            RosterEntryStatus.READY -> {
                when (next) {
                    RosterEntryStatus.DRAINING, RosterEntryStatus.STOPPED -> return true
                }
                return false
            }
            RosterEntryStatus.DRAINING -> {
                when (next) {
                    RosterEntryStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RosterEntryStatus) {
        if (!canMoveTo(next)) {
            error("roster entry $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveLoad(): Double = round((startsAt.toDouble() - retries.toDouble()) * 100) / 100

    fun snapshot(): RosterEntry {
        return RosterEntry(
            code = code,
            label = label,
            retries = retries,
            description = description,
            startsAt = startsAt,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a roster entry in status idle. */
fun newRosterEntry(code: String, label: String): RosterEntry {
    return RosterEntry(
        code = code,
        label = label,
        retries = 0,
        description = "",
        startsAt = 0,
        depot = "",
        status = RosterEntryStatus.IDLE,
    )
}
