package com.acme.transport.customs.clearance.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient clearance window is in its life cycle. */
enum class LenientClearanceWindowStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a lenient clearance window and its current status. */
data class LenientClearanceWindow(
    var code: String,
    var label: String,
    var budget: Double,
    var cost: Double,
    var description: String,
    var vehicleId: String,
    var graceDays: Int,
    var sequence: Int,
    var status: LenientClearanceWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientClearanceWindowStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientClearanceWindowStatus): Boolean {
        when (status) {
            LenientClearanceWindowStatus.IDLE -> return next == LenientClearanceWindowStatus.DRAINING || next == LenientClearanceWindowStatus.STOPPED
            LenientClearanceWindowStatus.WARMING -> return next == LenientClearanceWindowStatus.READY || next == LenientClearanceWindowStatus.DRAINING
            LenientClearanceWindowStatus.READY -> return next == LenientClearanceWindowStatus.DRAINING
            LenientClearanceWindowStatus.DRAINING -> return next == LenientClearanceWindowStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: LenientClearanceWindowStatus) {
        if (!canMoveTo(next)) {
            error("lenient clearance window $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedLoad(): Double = round((cost - graceDays.toDouble()) * 100) / 100

    fun snapshot(): LenientClearanceWindow {
        return LenientClearanceWindow(
            code = code,
            label = label,
            budget = budget,
            cost = cost,
            description = description,
            vehicleId = vehicleId,
            graceDays = graceDays,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lenient clearance window in status idle. */
fun newLenientClearanceWindow(code: String, label: String): LenientClearanceWindow {
    return LenientClearanceWindow(
        code = code,
        label = label,
        budget = 0.0,
        cost = 0.0,
        description = "",
        vehicleId = "",
        graceDays = 0,
        sequence = 0,
        status = LenientClearanceWindowStatus.IDLE,
    )
}
