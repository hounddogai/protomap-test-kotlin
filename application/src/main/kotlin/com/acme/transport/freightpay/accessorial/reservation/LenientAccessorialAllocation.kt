package com.acme.transport.freightpay.accessorial.reservation

import com.acme.transport.kit.text.normalize

/** Where a lenient accessorial allocation is in its life cycle. */
enum class LenientAccessorialAllocationStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A lenient accessorial allocation as the accessorial team keeps it. */
data class LenientAccessorialAllocation(
    var code: String,
    var name: String,
    var source: String,
    var quantity: Int,
    var timezone: String,
    var status: LenientAccessorialAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientAccessorialAllocationStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientAccessorialAllocationStatus): Boolean {
        when (status) {
            LenientAccessorialAllocationStatus.PLANNED -> {
                when (next) {
                    LenientAccessorialAllocationStatus.SCHEDULED, LenientAccessorialAllocationStatus.COMPLETED -> return true
                }
                return false
            }
            LenientAccessorialAllocationStatus.SCHEDULED -> {
                when (next) {
                    LenientAccessorialAllocationStatus.CANCELLED -> return true
                }
                return false
            }
            LenientAccessorialAllocationStatus.IN_PROGRESS -> {
                when (next) {
                    LenientAccessorialAllocationStatus.CANCELLED -> return true
                }
                return false
            }
            LenientAccessorialAllocationStatus.COMPLETED -> return next == LenientAccessorialAllocationStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LenientAccessorialAllocationStatus) {
        if (!canMoveTo(next)) {
            error("lenient accessorial allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): LenientAccessorialAllocation {
        return LenientAccessorialAllocation(
            code = code,
            name = name,
            source = source,
            quantity = quantity,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a lenient accessorial allocation in status planned. */
fun newLenientAccessorialAllocation(code: String, name: String): LenientAccessorialAllocation {
    return LenientAccessorialAllocation(
        code = code,
        name = name,
        source = "",
        quantity = 0,
        timezone = "",
        status = LenientAccessorialAllocationStatus.PLANNED,
    )
}
