package com.acme.transport.customs.hold.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hold cycle is in its life cycle. */
enum class HoldCycleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One hold cycle, with what the planners need to act on it. */
data class HoldCycle(
    var code: String,
    var label: String,
    var phone: String,
    var score: Double,
    var threshold: Double,
    var approved: Boolean,
    var amount: Double,
    var odometer: Int,
    var sequence: Int,
    var status: HoldCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldCycleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldCycleStatus): Boolean {
        when (status) {
            HoldCycleStatus.PLANNED -> return next == HoldCycleStatus.SCHEDULED
            HoldCycleStatus.SCHEDULED -> return next == HoldCycleStatus.COMPLETED
            HoldCycleStatus.IN_PROGRESS -> return next == HoldCycleStatus.COMPLETED || next == HoldCycleStatus.CANCELLED
            HoldCycleStatus.COMPLETED -> {
                when (next) {
                    HoldCycleStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: HoldCycleStatus) {
        if (!canMoveTo(next)) {
            error("hold cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveValue(): Double = round((score - amount) * 100) / 100

    fun snapshot(): HoldCycle {
        return HoldCycle(
            code = code,
            label = label,
            phone = phone,
            score = score,
            threshold = threshold,
            approved = approved,
            amount = amount,
            odometer = odometer,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a hold cycle in status planned. */
fun newHoldCycle(code: String, label: String): HoldCycle {
    return HoldCycle(
        code = code,
        label = label,
        phone = "",
        score = 0.0,
        threshold = 0.0,
        approved = false,
        amount = 0.0,
        odometer = 0,
        sequence = 0,
        status = HoldCycleStatus.PLANNED,
    )
}
