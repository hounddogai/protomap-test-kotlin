package com.acme.transport.safety.violation.violationcycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling violation cycle records of safety's violation work, and the rules, stores, and calculations
// around them.

/** Where a rolling violation cycle is in its life cycle. */
enum class RollingViolationCycleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A rolling violation cycle. Its code is unique within a tenant. */
data class RollingViolationCycle(
    var code: String,
    var title: String,
    var budget: Double,
    var visible: Boolean,
    var startsAt: Int,
    var status: RollingViolationCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingViolationCycleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingViolationCycleStatus): Boolean {
        when (status) {
            RollingViolationCycleStatus.IDLE -> return next == RollingViolationCycleStatus.STOPPED
            RollingViolationCycleStatus.WARMING -> return next == RollingViolationCycleStatus.READY || next == RollingViolationCycleStatus.DRAINING
            RollingViolationCycleStatus.READY -> return next == RollingViolationCycleStatus.DRAINING || next == RollingViolationCycleStatus.STOPPED
            RollingViolationCycleStatus.DRAINING -> return next == RollingViolationCycleStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RollingViolationCycleStatus) {
        if (!canMoveTo(next)) {
            error("rolling violation cycle $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"rolling violation cycle"} $code"

    fun adjustedTotal(): Double = round(startsAt.toDouble() * budget * 100) / 100

    fun snapshot(): RollingViolationCycle {
        return RollingViolationCycle(
            code = code,
            title = title,
            budget = budget,
            visible = visible,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling violation cycle in status idle. */
fun newRollingViolationCycle(code: String, title: String): RollingViolationCycle {
    return RollingViolationCycle(
        code = code,
        title = title,
        budget = 0.0,
        visible = false,
        startsAt = 0,
        status = RollingViolationCycleStatus.IDLE,
    )
}
