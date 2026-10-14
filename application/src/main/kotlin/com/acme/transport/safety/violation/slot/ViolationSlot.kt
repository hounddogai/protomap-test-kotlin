package com.acme.transport.safety.violation.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The violation slot records of safety's violation work, and the rules, stores, and calculations around them.

/** Where a violation slot is in its life cycle. */
enum class ViolationSlotStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One violation slot, with what the planners need to act on it. */
data class ViolationSlot(
    var code: String,
    var title: String,
    var channel: String,
    var stopCount: Int,
    var trailerId: String,
    var amount: Double,
    var status: ViolationSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationSlotStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationSlotStatus): Boolean {
        when (status) {
            ViolationSlotStatus.NEW -> return next == ViolationSlotStatus.RUNNING || next == ViolationSlotStatus.FAILED
            ViolationSlotStatus.QUEUED -> return next == ViolationSlotStatus.FAILED
            ViolationSlotStatus.RUNNING -> {
                when (next) {
                    ViolationSlotStatus.DONE, ViolationSlotStatus.FAILED -> return true
                }
                return false
            }
            ViolationSlotStatus.DONE -> return next == ViolationSlotStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ViolationSlotStatus) {
        if (!canMoveTo(next)) {
            error("violation slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun adjustedValue(): Double = round((amount + stopCount.toDouble()) * 100) / 100

    fun snapshot(): ViolationSlot {
        return ViolationSlot(
            code = code,
            title = title,
            channel = channel,
            stopCount = stopCount,
            trailerId = trailerId,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a violation slot in status new. */
fun newViolationSlot(code: String, title: String): ViolationSlot {
    return ViolationSlot(
        code = code,
        title = title,
        channel = "",
        stopCount = 0,
        trailerId = "",
        amount = 0.0,
        status = ViolationSlotStatus.NEW,
    )
}
