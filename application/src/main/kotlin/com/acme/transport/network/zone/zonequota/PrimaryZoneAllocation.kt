package com.acme.transport.network.zone.zonequota

import com.acme.transport.kit.text.normalize

/** Where a primary zone allocation is in its life cycle. */
enum class PrimaryZoneAllocationStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A primary zone allocation. Its code is unique within a tenant. */
data class PrimaryZoneAllocation(
    var code: String,
    var title: String,
    var visible: Boolean,
    var source: String,
    var graceDays: Int,
    var contactEmail: String,
    var notes: String,
    var status: PrimaryZoneAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryZoneAllocationStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryZoneAllocationStatus): Boolean {
        when (status) {
            PrimaryZoneAllocationStatus.PROPOSED -> return next == PrimaryZoneAllocationStatus.APPROVED || next == PrimaryZoneAllocationStatus.WITHDRAWN
            PrimaryZoneAllocationStatus.APPROVED -> return next == PrimaryZoneAllocationStatus.REJECTED || next == PrimaryZoneAllocationStatus.WITHDRAWN
            PrimaryZoneAllocationStatus.REJECTED -> return next == PrimaryZoneAllocationStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: PrimaryZoneAllocationStatus) {
        if (!canMoveTo(next)) {
            error("primary zone allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"primary zone allocation"} $code"

    fun measuredValue(): Double = graceDays.toDouble()

    fun snapshot(): PrimaryZoneAllocation {
        return PrimaryZoneAllocation(
            code = code,
            title = title,
            visible = visible,
            source = source,
            graceDays = graceDays,
            contactEmail = contactEmail,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a primary zone allocation in status proposed. */
fun newPrimaryZoneAllocation(code: String, title: String): PrimaryZoneAllocation {
    return PrimaryZoneAllocation(
        code = code,
        title = title,
        visible = false,
        source = "",
        graceDays = 0,
        contactEmail = "",
        notes = "",
        status = PrimaryZoneAllocationStatus.PROPOSED,
    )
}
