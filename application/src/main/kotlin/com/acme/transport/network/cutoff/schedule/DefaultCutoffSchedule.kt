package com.acme.transport.network.cutoff.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default cutoff schedule records of network's cutoff work, and the rules, stores, and calculations
// around them.

/** Where a default cutoff schedule is in its life cycle. */
enum class DefaultCutoffScheduleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A default cutoff schedule as the cutoff team keeps it. */
data class DefaultCutoffSchedule(
    var code: String,
    var label: String,
    var externalRef: String,
    var pallets: Int,
    var ceiling: Double,
    var shiftCode: String,
    var ratio: Double,
    var status: DefaultCutoffScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultCutoffScheduleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultCutoffScheduleStatus): Boolean {
        when (status) {
            DefaultCutoffScheduleStatus.IDLE -> {
                when (next) {
                    DefaultCutoffScheduleStatus.WARMING, DefaultCutoffScheduleStatus.READY -> return true
                }
                return false
            }
            DefaultCutoffScheduleStatus.WARMING -> {
                when (next) {
                    DefaultCutoffScheduleStatus.DRAINING -> return true
                }
                return false
            }
            DefaultCutoffScheduleStatus.READY -> return next == DefaultCutoffScheduleStatus.DRAINING
            DefaultCutoffScheduleStatus.DRAINING -> return next == DefaultCutoffScheduleStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: DefaultCutoffScheduleStatus) {
        if (!canMoveTo(next)) {
            error("default cutoff schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"default cutoff schedule"} $code"

    fun weightedExposure(): Double = round((pallets.toDouble() + ratio) * 100) / 100

    fun snapshot(): DefaultCutoffSchedule {
        return DefaultCutoffSchedule(
            code = code,
            label = label,
            externalRef = externalRef,
            pallets = pallets,
            ceiling = ceiling,
            shiftCode = shiftCode,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a default cutoff schedule in status idle. */
fun newDefaultCutoffSchedule(code: String, label: String): DefaultCutoffSchedule {
    return DefaultCutoffSchedule(
        code = code,
        label = label,
        externalRef = "",
        pallets = 0,
        ceiling = 0.0,
        shiftCode = "",
        ratio = 0.0,
        status = DefaultCutoffScheduleStatus.IDLE,
    )
}
