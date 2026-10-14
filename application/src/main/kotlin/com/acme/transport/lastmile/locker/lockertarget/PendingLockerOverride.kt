package com.acme.transport.lastmile.locker.lockertarget

import com.acme.transport.kit.text.normalize

/** Where a pending locker override is in its life cycle. */
enum class PendingLockerOverrideStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A pending locker override as the locker team keeps it. */
data class PendingLockerOverride(
    var code: String,
    var title: String,
    var currency: String,
    var region: String,
    var shiftCode: String,
    var carrier: String,
    var externalRef: String,
    var reviewer: String,
    var quantity: Int,
    var status: PendingLockerOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingLockerOverrideStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingLockerOverrideStatus): Boolean {
        when (status) {
            PendingLockerOverrideStatus.PLANNED -> return next == PendingLockerOverrideStatus.IN_PROGRESS || next == PendingLockerOverrideStatus.CANCELLED
            PendingLockerOverrideStatus.SCHEDULED -> return next == PendingLockerOverrideStatus.COMPLETED
            PendingLockerOverrideStatus.IN_PROGRESS -> return next == PendingLockerOverrideStatus.CANCELLED
            PendingLockerOverrideStatus.COMPLETED -> return next == PendingLockerOverrideStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: PendingLockerOverrideStatus) {
        if (!canMoveTo(next)) {
            error("pending locker override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): PendingLockerOverride {
        return PendingLockerOverride(
            code = code,
            title = title,
            currency = currency,
            region = region,
            shiftCode = shiftCode,
            carrier = carrier,
            externalRef = externalRef,
            reviewer = reviewer,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a pending locker override in status planned. */
fun newPendingLockerOverride(code: String, title: String): PendingLockerOverride {
    return PendingLockerOverride(
        code = code,
        title = title,
        currency = "",
        region = "",
        shiftCode = "",
        carrier = "",
        externalRef = "",
        reviewer = "",
        quantity = 0,
        status = PendingLockerOverrideStatus.PLANNED,
    )
}
