package com.acme.transport.safety.violation.violationslot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional violation slot records of safety's violation work, and the rules, stores, and calculations
// around them.

/** Where a regional violation slot is in its life cycle. */
enum class RegionalViolationSlotStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One regional violation slot, with what the planners need to act on it. */
data class RegionalViolationSlot(
    var code: String,
    var label: String,
    var startsAt: Int,
    var ceiling: Double,
    var channel: String,
    var revision: Int,
    var status: RegionalViolationSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalViolationSlotStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalViolationSlotStatus): Boolean {
        when (status) {
            RegionalViolationSlotStatus.NEW -> return next == RegionalViolationSlotStatus.DONE
            RegionalViolationSlotStatus.QUEUED -> {
                when (next) {
                    RegionalViolationSlotStatus.DONE -> return true
                }
                return false
            }
            RegionalViolationSlotStatus.RUNNING -> return next == RegionalViolationSlotStatus.DONE || next == RegionalViolationSlotStatus.FAILED
            RegionalViolationSlotStatus.DONE -> {
                when (next) {
                    RegionalViolationSlotStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalViolationSlotStatus) {
        if (!canMoveTo(next)) {
            error("regional violation slot $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedValue(): Double = round((revision.toDouble() - startsAt.toDouble()) * 100) / 100

    fun snapshot(): RegionalViolationSlot {
        return RegionalViolationSlot(
            code = code,
            label = label,
            startsAt = startsAt,
            ceiling = ceiling,
            channel = channel,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a regional violation slot in status new. */
fun newRegionalViolationSlot(code: String, label: String): RegionalViolationSlot {
    return RegionalViolationSlot(
        code = code,
        label = label,
        startsAt = 0,
        ceiling = 0.0,
        channel = "",
        revision = 0,
        status = RegionalViolationSlotStatus.NEW,
    )
}
