package com.acme.transport.safety.violation.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy violation cycle is in its life cycle. */
enum class LegacyViolationCycleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One legacy violation cycle, with what the planners need to act on it. */
data class LegacyViolationCycle(
    var code: String,
    var label: String,
    var capacity: Int,
    var routeCode: String,
    var weight: Double,
    var title: String,
    var dock: String,
    var status: LegacyViolationCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyViolationCycleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyViolationCycleStatus): Boolean {
        when (status) {
            LegacyViolationCycleStatus.IDLE -> return next == LegacyViolationCycleStatus.WARMING || next == LegacyViolationCycleStatus.STOPPED
            LegacyViolationCycleStatus.WARMING -> {
                when (next) {
                    LegacyViolationCycleStatus.STOPPED -> return true
                }
                return false
            }
            LegacyViolationCycleStatus.READY -> return next == LegacyViolationCycleStatus.DRAINING
            LegacyViolationCycleStatus.DRAINING -> {
                when (next) {
                    LegacyViolationCycleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyViolationCycleStatus) {
        if (!canMoveTo(next)) {
            error("legacy violation cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedLoad(): Double = round(capacity.toDouble() * weight * 100) / 100

    fun snapshot(): LegacyViolationCycle {
        return LegacyViolationCycle(
            code = code,
            label = label,
            capacity = capacity,
            routeCode = routeCode,
            weight = weight,
            title = title,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a legacy violation cycle in status idle. */
fun newLegacyViolationCycle(code: String, label: String): LegacyViolationCycle {
    return LegacyViolationCycle(
        code = code,
        label = label,
        capacity = 0,
        routeCode = "",
        weight = 0.0,
        title = "",
        dock = "",
        status = LegacyViolationCycleStatus.IDLE,
    )
}
