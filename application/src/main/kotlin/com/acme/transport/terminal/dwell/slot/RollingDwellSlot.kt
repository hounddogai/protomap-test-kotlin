package com.acme.transport.terminal.dwell.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling dwell slot records of terminal's dwell work, and the rules, stores, and calculations around
// them.

/** Where a rolling dwell slot is in its life cycle. */
enum class RollingDwellSlotStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One rolling dwell slot, with what the planners need to act on it. */
data class RollingDwellSlot(
    var code: String,
    var title: String,
    var summary: String,
    var retries: Int,
    var maximum: Double,
    var currency: String,
    var quantity: Int,
    var label: String,
    var externalRef: String,
    var graceDays: Int,
    var status: RollingDwellSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingDwellSlotStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingDwellSlotStatus): Boolean {
        when (status) {
            RollingDwellSlotStatus.PLANNED -> {
                when (next) {
                    RollingDwellSlotStatus.CANCELLED -> return true
                }
                return false
            }
            RollingDwellSlotStatus.SCHEDULED -> return next == RollingDwellSlotStatus.IN_PROGRESS || next == RollingDwellSlotStatus.CANCELLED
            RollingDwellSlotStatus.IN_PROGRESS -> {
                when (next) {
                    RollingDwellSlotStatus.COMPLETED, RollingDwellSlotStatus.CANCELLED -> return true
                }
                return false
            }
            RollingDwellSlotStatus.COMPLETED -> {
                when (next) {
                    RollingDwellSlotStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingDwellSlotStatus) {
        if (!canMoveTo(next)) {
            error("rolling dwell slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($title)"

    fun projectedVolume(): Double = round((graceDays.toDouble() + quantity.toDouble()) * 100) / 100

    fun snapshot(): RollingDwellSlot {
        return RollingDwellSlot(
            code = code,
            title = title,
            summary = summary,
            retries = retries,
            maximum = maximum,
            currency = currency,
            quantity = quantity,
            label = label,
            externalRef = externalRef,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling dwell slot in status planned. */
fun newRollingDwellSlot(code: String, title: String): RollingDwellSlot {
    return RollingDwellSlot(
        code = code,
        title = title,
        summary = "",
        retries = 0,
        maximum = 0.0,
        currency = "",
        quantity = 0,
        label = "",
        externalRef = "",
        graceDays = 0,
        status = RollingDwellSlotStatus.PLANNED,
    )
}
