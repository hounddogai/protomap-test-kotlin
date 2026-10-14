package com.acme.transport.freightpay.dispute.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dispute slot is in its life cycle. */
enum class DisputeSlotStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One dispute slot, with what the planners need to act on it. */
data class DisputeSlot(
    var code: String,
    var name: String,
    var externalRef: String,
    var revision: Int,
    var budget: Double,
    var graceDays: Int,
    var status: DisputeSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputeSlotStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputeSlotStatus): Boolean {
        when (status) {
            DisputeSlotStatus.NEW -> {
                when (next) {
                    DisputeSlotStatus.DONE -> return true
                }
                return false
            }
            DisputeSlotStatus.QUEUED -> return next == DisputeSlotStatus.FAILED
            DisputeSlotStatus.RUNNING -> {
                when (next) {
                    DisputeSlotStatus.DONE, DisputeSlotStatus.FAILED -> return true
                }
                return false
            }
            DisputeSlotStatus.DONE -> {
                when (next) {
                    DisputeSlotStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DisputeSlotStatus) {
        if (!canMoveTo(next)) {
            error("dispute slot $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"dispute slot"} $code"

    fun adjustedExposure(): Double = round(revision.toDouble() * graceDays.toDouble() * 100) / 100

    fun snapshot(): DisputeSlot {
        return DisputeSlot(
            code = code,
            name = name,
            externalRef = externalRef,
            revision = revision,
            budget = budget,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a dispute slot in status new. */
fun newDisputeSlot(code: String, name: String): DisputeSlot {
    return DisputeSlot(
        code = code,
        name = name,
        externalRef = "",
        revision = 0,
        budget = 0.0,
        graceDays = 0,
        status = DisputeSlotStatus.NEW,
    )
}
