package com.acme.transport.freightpay.accessorial.entry

import com.acme.transport.kit.text.normalize

/** Where a rolling accessorial allocation is in its life cycle. */
enum class RollingAccessorialAllocationStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A rolling accessorial allocation as the accessorial team keeps it. */
data class RollingAccessorialAllocation(
    var code: String,
    var label: String,
    var trailerId: String,
    var contactEmail: String,
    var routeCode: String,
    var checksum: String,
    var quantity: Int,
    var status: RollingAccessorialAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingAccessorialAllocationStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingAccessorialAllocationStatus): Boolean {
        when (status) {
            RollingAccessorialAllocationStatus.NEW -> return next == RollingAccessorialAllocationStatus.QUEUED
            RollingAccessorialAllocationStatus.QUEUED -> {
                when (next) {
                    RollingAccessorialAllocationStatus.RUNNING, RollingAccessorialAllocationStatus.DONE -> return true
                }
                return false
            }
            RollingAccessorialAllocationStatus.RUNNING -> {
                when (next) {
                    RollingAccessorialAllocationStatus.FAILED -> return true
                }
                return false
            }
            RollingAccessorialAllocationStatus.DONE -> return next == RollingAccessorialAllocationStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RollingAccessorialAllocationStatus) {
        if (!canMoveTo(next)) {
            error("rolling accessorial allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): RollingAccessorialAllocation {
        return RollingAccessorialAllocation(
            code = code,
            label = label,
            trailerId = trailerId,
            contactEmail = contactEmail,
            routeCode = routeCode,
            checksum = checksum,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rolling accessorial allocation in status new. */
fun newRollingAccessorialAllocation(code: String, label: String): RollingAccessorialAllocation {
    return RollingAccessorialAllocation(
        code = code,
        label = label,
        trailerId = "",
        contactEmail = "",
        routeCode = "",
        checksum = "",
        quantity = 0,
        status = RollingAccessorialAllocationStatus.NEW,
    )
}
