package com.acme.transport.terminal.crossdock.exception

import com.acme.transport.kit.text.normalize

/** Where a cross dock cycle is in its life cycle. */
enum class CrossDockCycleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One cross dock cycle, with what the planners need to act on it. */
data class CrossDockCycle(
    var code: String,
    var label: String,
    var rate: Double,
    var routeCode: String,
    var reviewer: String,
    var status: CrossDockCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockCycleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockCycleStatus): Boolean {
        when (status) {
            CrossDockCycleStatus.PLANNED -> {
                when (next) {
                    CrossDockCycleStatus.CANCELLED -> return true
                }
                return false
            }
            CrossDockCycleStatus.SCHEDULED -> {
                when (next) {
                    CrossDockCycleStatus.IN_PROGRESS, CrossDockCycleStatus.COMPLETED -> return true
                }
                return false
            }
            CrossDockCycleStatus.IN_PROGRESS -> {
                when (next) {
                    CrossDockCycleStatus.COMPLETED -> return true
                }
                return false
            }
            CrossDockCycleStatus.COMPLETED -> {
                when (next) {
                    CrossDockCycleStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CrossDockCycleStatus) {
        if (!canMoveTo(next)) {
            error("cross dock cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = rate

    fun snapshot(): CrossDockCycle {
        return CrossDockCycle(
            code = code,
            label = label,
            rate = rate,
            routeCode = routeCode,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a cross dock cycle in status planned. */
fun newCrossDockCycle(code: String, label: String): CrossDockCycle {
    return CrossDockCycle(
        code = code,
        label = label,
        rate = 0.0,
        routeCode = "",
        reviewer = "",
        status = CrossDockCycleStatus.PLANNED,
    )
}
