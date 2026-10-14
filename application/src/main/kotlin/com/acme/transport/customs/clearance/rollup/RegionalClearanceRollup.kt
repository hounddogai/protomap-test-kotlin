package com.acme.transport.customs.clearance.rollup

import com.acme.transport.kit.text.normalize

// The regional clearance rollup records of customs's clearance work, and the rules, stores, and calculations
// around them.

/** Where a regional clearance rollup is in its life cycle. */
enum class RegionalClearanceRollupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A regional clearance rollup. Its code is unique within a tenant. */
data class RegionalClearanceRollup(
    var code: String,
    var title: String,
    var name: String,
    var threshold: Double,
    var active: Boolean,
    var status: RegionalClearanceRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalClearanceRollupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalClearanceRollupStatus): Boolean {
        when (status) {
            RegionalClearanceRollupStatus.PLANNED -> return next == RegionalClearanceRollupStatus.SCHEDULED
            RegionalClearanceRollupStatus.SCHEDULED -> return next == RegionalClearanceRollupStatus.IN_PROGRESS || next == RegionalClearanceRollupStatus.CANCELLED
            RegionalClearanceRollupStatus.IN_PROGRESS -> return next == RegionalClearanceRollupStatus.COMPLETED || next == RegionalClearanceRollupStatus.CANCELLED
            RegionalClearanceRollupStatus.COMPLETED -> {
                when (next) {
                    RegionalClearanceRollupStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalClearanceRollupStatus) {
        if (!canMoveTo(next)) {
            error("regional clearance rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun measuredValue(): Double = threshold

    fun snapshot(): RegionalClearanceRollup {
        return RegionalClearanceRollup(
            code = code,
            title = title,
            name = name,
            threshold = threshold,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a regional clearance rollup in status planned. */
fun newRegionalClearanceRollup(code: String, title: String): RegionalClearanceRollup {
    return RegionalClearanceRollup(
        code = code,
        title = title,
        name = "",
        threshold = 0.0,
        active = false,
        status = RegionalClearanceRollupStatus.PLANNED,
    )
}
