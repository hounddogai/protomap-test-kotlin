package com.acme.transport.customs.hold.holdsummary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient hold schedule is in its life cycle. */
enum class LenientHoldScheduleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A lenient hold schedule as the hold team keeps it. */
data class LenientHoldSchedule(
    var code: String,
    var name: String,
    var pallets: Int,
    var maximum: Double,
    var checksum: String,
    var status: LenientHoldScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientHoldScheduleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientHoldScheduleStatus): Boolean {
        when (status) {
            LenientHoldScheduleStatus.PROPOSED -> return next == LenientHoldScheduleStatus.REJECTED
            LenientHoldScheduleStatus.APPROVED -> return next == LenientHoldScheduleStatus.REJECTED || next == LenientHoldScheduleStatus.WITHDRAWN
            LenientHoldScheduleStatus.REJECTED -> return next == LenientHoldScheduleStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: LenientHoldScheduleStatus) {
        if (!canMoveTo(next)) {
            error("lenient hold schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun adjustedExposure(): Double = round(pallets.toDouble() * maximum * 100) / 100

    fun snapshot(): LenientHoldSchedule {
        return LenientHoldSchedule(
            code = code,
            name = name,
            pallets = pallets,
            maximum = maximum,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lenient hold schedule in status proposed. */
fun newLenientHoldSchedule(code: String, name: String): LenientHoldSchedule {
    return LenientHoldSchedule(
        code = code,
        name = name,
        pallets = 0,
        maximum = 0.0,
        checksum = "",
        status = LenientHoldScheduleStatus.PROPOSED,
    )
}
