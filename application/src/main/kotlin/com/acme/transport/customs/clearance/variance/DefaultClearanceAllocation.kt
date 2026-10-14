package com.acme.transport.customs.clearance.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default clearance allocation is in its life cycle. */
enum class DefaultClearanceAllocationStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a default clearance allocation and its current status. */
data class DefaultClearanceAllocation(
    var code: String,
    var label: String,
    var externalRef: String,
    var channel: String,
    var floorValue: Double,
    var driverId: String,
    var amount: Double,
    var discount: Double,
    var minimum: Double,
    var updatedAt: Int,
    var currency: String,
    var status: DefaultClearanceAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultClearanceAllocationStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultClearanceAllocationStatus): Boolean {
        when (status) {
            DefaultClearanceAllocationStatus.REQUESTED -> return next == DefaultClearanceAllocationStatus.REVOKED
            DefaultClearanceAllocationStatus.GRANTED -> {
                when (next) {
                    DefaultClearanceAllocationStatus.REVOKED, DefaultClearanceAllocationStatus.EXPIRED -> return true
                }
                return false
            }
            DefaultClearanceAllocationStatus.REVOKED -> return next == DefaultClearanceAllocationStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: DefaultClearanceAllocationStatus) {
        if (!canMoveTo(next)) {
            error("default clearance allocation $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveValue(): Double = round((amount - minimum) * 100) / 100

    fun snapshot(): DefaultClearanceAllocation {
        return DefaultClearanceAllocation(
            code = code,
            label = label,
            externalRef = externalRef,
            channel = channel,
            floorValue = floorValue,
            driverId = driverId,
            amount = amount,
            discount = discount,
            minimum = minimum,
            updatedAt = updatedAt,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a default clearance allocation in status requested. */
fun newDefaultClearanceAllocation(code: String, label: String): DefaultClearanceAllocation {
    return DefaultClearanceAllocation(
        code = code,
        label = label,
        externalRef = "",
        channel = "",
        floorValue = 0.0,
        driverId = "",
        amount = 0.0,
        discount = 0.0,
        minimum = 0.0,
        updatedAt = 0,
        currency = "",
        status = DefaultClearanceAllocationStatus.REQUESTED,
    )
}
