package com.acme.transport.fleetops.utilization.utilizationreservation

import com.acme.transport.kit.text.normalize

/** Where a rolling utilization target is in its life cycle. */
enum class RollingUtilizationTargetStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a rolling utilization target and its current status. */
data class RollingUtilizationTarget(
    var code: String,
    var name: String,
    var notes: String,
    var unit: String,
    var currency: String,
    var etaMinutes: Int,
    var dock: String,
    var status: RollingUtilizationTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingUtilizationTargetStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingUtilizationTargetStatus): Boolean {
        when (status) {
            RollingUtilizationTargetStatus.DRAFT -> {
                when (next) {
                    RollingUtilizationTargetStatus.ACTIVE, RollingUtilizationTargetStatus.CLOSED -> return true
                }
                return false
            }
            RollingUtilizationTargetStatus.PENDING -> {
                when (next) {
                    RollingUtilizationTargetStatus.ACTIVE, RollingUtilizationTargetStatus.SUSPENDED -> return true
                }
                return false
            }
            RollingUtilizationTargetStatus.ACTIVE -> {
                when (next) {
                    RollingUtilizationTargetStatus.SUSPENDED -> return true
                }
                return false
            }
            RollingUtilizationTargetStatus.SUSPENDED -> {
                when (next) {
                    RollingUtilizationTargetStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingUtilizationTargetStatus) {
        if (!canMoveTo(next)) {
            error("rolling utilization target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = etaMinutes.toDouble()

    fun snapshot(): RollingUtilizationTarget {
        return RollingUtilizationTarget(
            code = code,
            name = name,
            notes = notes,
            unit = unit,
            currency = currency,
            etaMinutes = etaMinutes,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a rolling utilization target in status draft. */
fun newRollingUtilizationTarget(code: String, name: String): RollingUtilizationTarget {
    return RollingUtilizationTarget(
        code = code,
        name = name,
        notes = "",
        unit = "",
        currency = "",
        etaMinutes = 0,
        dock = "",
        status = RollingUtilizationTargetStatus.DRAFT,
    )
}
