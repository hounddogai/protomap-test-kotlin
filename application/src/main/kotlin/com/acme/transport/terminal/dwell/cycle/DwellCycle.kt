package com.acme.transport.terminal.dwell.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The dwell cycle records of terminal's dwell work, and the rules, stores, and calculations around them.

/** Where a dwell cycle is in its life cycle. */
enum class DwellCycleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A dwell cycle as the dwell team keeps it. */
data class DwellCycle(
    var code: String,
    var name: String,
    var ceiling: Double,
    var graceDays: Int,
    var durationMinutes: Int,
    var label: String,
    var timezone: String,
    var routeCode: String,
    var status: DwellCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellCycleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellCycleStatus): Boolean {
        when (status) {
            DwellCycleStatus.NEW -> {
                when (next) {
                    DwellCycleStatus.RUNNING -> return true
                }
                return false
            }
            DwellCycleStatus.QUEUED -> return next == DwellCycleStatus.DONE
            DwellCycleStatus.RUNNING -> {
                when (next) {
                    DwellCycleStatus.FAILED -> return true
                }
                return false
            }
            DwellCycleStatus.DONE -> {
                when (next) {
                    DwellCycleStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DwellCycleStatus) {
        if (!canMoveTo(next)) {
            error("dwell cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"dwell cycle"} $code"

    fun netLoad(): Double = round(graceDays.toDouble() * durationMinutes.toDouble() * 100) / 100

    fun snapshot(): DwellCycle {
        return DwellCycle(
            code = code,
            name = name,
            ceiling = ceiling,
            graceDays = graceDays,
            durationMinutes = durationMinutes,
            label = label,
            timezone = timezone,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a dwell cycle in status new. */
fun newDwellCycle(code: String, name: String): DwellCycle {
    return DwellCycle(
        code = code,
        name = name,
        ceiling = 0.0,
        graceDays = 0,
        durationMinutes = 0,
        label = "",
        timezone = "",
        routeCode = "",
        status = DwellCycleStatus.NEW,
    )
}
