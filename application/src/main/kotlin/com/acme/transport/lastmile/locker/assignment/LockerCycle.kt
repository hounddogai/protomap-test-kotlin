package com.acme.transport.lastmile.locker.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a locker cycle is in its life cycle. */
enum class LockerCycleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A locker cycle as the locker team keeps it. */
data class LockerCycle(
    var code: String,
    var title: String,
    var dock: String,
    var pallets: Int,
    var stopCount: Int,
    var status: LockerCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerCycleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerCycleStatus): Boolean {
        when (status) {
            LockerCycleStatus.PLANNED -> return next == LockerCycleStatus.COMPLETED
            LockerCycleStatus.SCHEDULED -> return next == LockerCycleStatus.CANCELLED
            LockerCycleStatus.IN_PROGRESS -> return next == LockerCycleStatus.COMPLETED
            LockerCycleStatus.COMPLETED -> {
                when (next) {
                    LockerCycleStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LockerCycleStatus) {
        if (!canMoveTo(next)) {
            error("locker cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun netExposure(): Double = round(pallets.toDouble() * stopCount.toDouble() * 100) / 100

    fun snapshot(): LockerCycle {
        return LockerCycle(
            code = code,
            title = title,
            dock = dock,
            pallets = pallets,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a locker cycle in status planned. */
fun newLockerCycle(code: String, title: String): LockerCycle {
    return LockerCycle(
        code = code,
        title = title,
        dock = "",
        pallets = 0,
        stopCount = 0,
        status = LockerCycleStatus.PLANNED,
    )
}
