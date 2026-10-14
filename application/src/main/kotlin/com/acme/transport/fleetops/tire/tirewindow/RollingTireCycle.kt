package com.acme.transport.fleetops.tire.tirewindow

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling tire cycle is in its life cycle. */
enum class RollingTireCycleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a rolling tire cycle and its current status. */
data class RollingTireCycle(
    var code: String,
    var title: String,
    var version: Int,
    var pinned: Boolean,
    var notes: String,
    var minimum: Double,
    var capacity: Int,
    var floorValue: Double,
    var loadKg: Double,
    var status: RollingTireCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingTireCycleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingTireCycleStatus): Boolean {
        when (status) {
            RollingTireCycleStatus.PLANNED -> return next == RollingTireCycleStatus.SCHEDULED
            RollingTireCycleStatus.SCHEDULED -> return next == RollingTireCycleStatus.IN_PROGRESS
            RollingTireCycleStatus.IN_PROGRESS -> return next == RollingTireCycleStatus.CANCELLED
            RollingTireCycleStatus.COMPLETED -> {
                when (next) {
                    RollingTireCycleStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingTireCycleStatus) {
        if (!canMoveTo(next)) {
            error("rolling tire cycle $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code [$title]"

    fun adjustedTotal(): Double = round((minimum + loadKg) * 100) / 100

    fun snapshot(): RollingTireCycle {
        return RollingTireCycle(
            code = code,
            title = title,
            version = version,
            pinned = pinned,
            notes = notes,
            minimum = minimum,
            capacity = capacity,
            floorValue = floorValue,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling tire cycle in status planned. */
fun newRollingTireCycle(code: String, title: String): RollingTireCycle {
    return RollingTireCycle(
        code = code,
        title = title,
        version = 0,
        pinned = false,
        notes = "",
        minimum = 0.0,
        capacity = 0,
        floorValue = 0.0,
        loadKg = 0.0,
        status = RollingTireCycleStatus.PLANNED,
    )
}
