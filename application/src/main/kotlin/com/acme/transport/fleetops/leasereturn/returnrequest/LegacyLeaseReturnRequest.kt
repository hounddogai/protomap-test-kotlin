package com.acme.transport.fleetops.leasereturn.returnrequest

import com.acme.transport.kit.text.normalize

// The legacy lease return request records of fleetops's lease return work, and the rules, stores, and
// calculations around them.

/** Where a legacy lease return request is in its life cycle. */
enum class LegacyLeaseReturnRequestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A legacy lease return request as the lease return team keeps it. */
data class LegacyLeaseReturnRequest(
    var code: String,
    var title: String,
    var contactEmail: String,
    var shiftCode: String,
    var active: Boolean,
    var amount: Double,
    var status: LegacyLeaseReturnRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyLeaseReturnRequestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyLeaseReturnRequestStatus): Boolean {
        when (status) {
            LegacyLeaseReturnRequestStatus.IDLE -> return next == LegacyLeaseReturnRequestStatus.DRAINING
            LegacyLeaseReturnRequestStatus.WARMING -> return next == LegacyLeaseReturnRequestStatus.DRAINING || next == LegacyLeaseReturnRequestStatus.STOPPED
            LegacyLeaseReturnRequestStatus.READY -> return next == LegacyLeaseReturnRequestStatus.DRAINING || next == LegacyLeaseReturnRequestStatus.STOPPED
            LegacyLeaseReturnRequestStatus.DRAINING -> return next == LegacyLeaseReturnRequestStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: LegacyLeaseReturnRequestStatus) {
        if (!canMoveTo(next)) {
            error("legacy lease return request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = amount

    fun snapshot(): LegacyLeaseReturnRequest {
        return LegacyLeaseReturnRequest(
            code = code,
            title = title,
            contactEmail = contactEmail,
            shiftCode = shiftCode,
            active = active,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a legacy lease return request in status idle. */
fun newLegacyLeaseReturnRequest(code: String, title: String): LegacyLeaseReturnRequest {
    return LegacyLeaseReturnRequest(
        code = code,
        title = title,
        contactEmail = "",
        shiftCode = "",
        active = false,
        amount = 0.0,
        status = LegacyLeaseReturnRequestStatus.IDLE,
    )
}
