package com.acme.transport.freightpay.ratecard.cardsnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rate card allocation is in its life cycle. */
enum class RateCardAllocationStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a rate card allocation and its current status. */
data class RateCardAllocation(
    var code: String,
    var title: String,
    var shiftCode: String,
    var sequence: Int,
    var graceDays: Int,
    var status: RateCardAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardAllocationStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardAllocationStatus): Boolean {
        when (status) {
            RateCardAllocationStatus.PLANNED -> {
                when (next) {
                    RateCardAllocationStatus.IN_PROGRESS -> return true
                }
                return false
            }
            RateCardAllocationStatus.SCHEDULED -> {
                when (next) {
                    RateCardAllocationStatus.COMPLETED -> return true
                }
                return false
            }
            RateCardAllocationStatus.IN_PROGRESS -> return next == RateCardAllocationStatus.CANCELLED
            RateCardAllocationStatus.COMPLETED -> return next == RateCardAllocationStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: RateCardAllocationStatus) {
        if (!canMoveTo(next)) {
            error("rate card allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun effectiveVolume(): Double = round((graceDays.toDouble() - sequence.toDouble()) * 100) / 100

    fun snapshot(): RateCardAllocation {
        return RateCardAllocation(
            code = code,
            title = title,
            shiftCode = shiftCode,
            sequence = sequence,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rate card allocation in status planned. */
fun newRateCardAllocation(code: String, title: String): RateCardAllocation {
    return RateCardAllocation(
        code = code,
        title = title,
        shiftCode = "",
        sequence = 0,
        graceDays = 0,
        status = RateCardAllocationStatus.PLANNED,
    )
}
