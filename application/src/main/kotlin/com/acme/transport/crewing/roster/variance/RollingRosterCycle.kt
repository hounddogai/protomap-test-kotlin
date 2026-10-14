package com.acme.transport.crewing.roster.variance

import com.acme.transport.kit.text.normalize

/** Where a rolling roster cycle is in its life cycle. */
enum class RollingRosterCycleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A rolling roster cycle. Its code is unique within a tenant. */
data class RollingRosterCycle(
    var code: String,
    var name: String,
    var budget: Double,
    var locale: String,
    var channel: String,
    var status: RollingRosterCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingRosterCycleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingRosterCycleStatus): Boolean {
        when (status) {
            RollingRosterCycleStatus.PLANNED -> {
                when (next) {
                    RollingRosterCycleStatus.IN_PROGRESS, RollingRosterCycleStatus.COMPLETED -> return true
                }
                return false
            }
            RollingRosterCycleStatus.SCHEDULED -> {
                when (next) {
                    RollingRosterCycleStatus.IN_PROGRESS, RollingRosterCycleStatus.CANCELLED -> return true
                }
                return false
            }
            RollingRosterCycleStatus.IN_PROGRESS -> return next == RollingRosterCycleStatus.COMPLETED || next == RollingRosterCycleStatus.CANCELLED
            RollingRosterCycleStatus.COMPLETED -> {
                when (next) {
                    RollingRosterCycleStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingRosterCycleStatus) {
        if (!canMoveTo(next)) {
            error("rolling roster cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"rolling roster cycle"} $code"

    fun measuredValue(): Double = budget

    fun snapshot(): RollingRosterCycle {
        return RollingRosterCycle(
            code = code,
            name = name,
            budget = budget,
            locale = locale,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a rolling roster cycle in status planned. */
fun newRollingRosterCycle(code: String, name: String): RollingRosterCycle {
    return RollingRosterCycle(
        code = code,
        name = name,
        budget = 0.0,
        locale = "",
        channel = "",
        status = RollingRosterCycleStatus.PLANNED,
    )
}
