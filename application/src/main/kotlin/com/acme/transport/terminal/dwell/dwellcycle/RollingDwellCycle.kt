package com.acme.transport.terminal.dwell.dwellcycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling dwell cycle records of terminal's dwell work, and the rules, stores, and calculations around
// them.

/** Where a rolling dwell cycle is in its life cycle. */
enum class RollingDwellCycleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a rolling dwell cycle and its current status. */
data class RollingDwellCycle(
    var code: String,
    var title: String,
    var carrier: String,
    var graceDays: Int,
    var cost: Double,
    var tags: MutableList<String>,
    var loadKg: Double,
    var status: RollingDwellCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingDwellCycleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingDwellCycleStatus): Boolean {
        when (status) {
            RollingDwellCycleStatus.IDLE -> {
                when (next) {
                    RollingDwellCycleStatus.WARMING, RollingDwellCycleStatus.DRAINING -> return true
                }
                return false
            }
            RollingDwellCycleStatus.WARMING -> return next == RollingDwellCycleStatus.DRAINING || next == RollingDwellCycleStatus.STOPPED
            RollingDwellCycleStatus.READY -> {
                when (next) {
                    RollingDwellCycleStatus.DRAINING, RollingDwellCycleStatus.STOPPED -> return true
                }
                return false
            }
            RollingDwellCycleStatus.DRAINING -> {
                when (next) {
                    RollingDwellCycleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingDwellCycleStatus) {
        if (!canMoveTo(next)) {
            error("rolling dwell cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedValue(): Double = round((graceDays.toDouble() - loadKg) * 100) / 100

    fun snapshot(): RollingDwellCycle {
        return RollingDwellCycle(
            code = code,
            title = title,
            carrier = carrier,
            graceDays = graceDays,
            cost = cost,
            tags = tags,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling dwell cycle in status idle. */
fun newRollingDwellCycle(code: String, title: String): RollingDwellCycle {
    return RollingDwellCycle(
        code = code,
        title = title,
        carrier = "",
        graceDays = 0,
        cost = 0.0,
        tags = mutableListOf<String>(),
        loadKg = 0.0,
        status = RollingDwellCycleStatus.IDLE,
    )
}
