package com.acme.transport.network.servicearea.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending service area allocation is in its life cycle. */
enum class PendingServiceAreaAllocationStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a pending service area allocation and its current status. */
data class PendingServiceAreaAllocation(
    var code: String,
    var name: String,
    var depot: String,
    var version: Int,
    var score: Double,
    var phone: String,
    var status: PendingServiceAreaAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingServiceAreaAllocationStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingServiceAreaAllocationStatus): Boolean {
        when (status) {
            PendingServiceAreaAllocationStatus.PROPOSED -> return next == PendingServiceAreaAllocationStatus.REJECTED
            PendingServiceAreaAllocationStatus.APPROVED -> return next == PendingServiceAreaAllocationStatus.REJECTED
            PendingServiceAreaAllocationStatus.REJECTED -> return next == PendingServiceAreaAllocationStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: PendingServiceAreaAllocationStatus) {
        if (!canMoveTo(next)) {
            error("pending service area allocation $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code ($name)"

    fun netExposure(): Double = round((version.toDouble() - score) * 100) / 100

    fun snapshot(): PendingServiceAreaAllocation {
        return PendingServiceAreaAllocation(
            code = code,
            name = name,
            depot = depot,
            version = version,
            score = score,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a pending service area allocation in status proposed. */
fun newPendingServiceAreaAllocation(code: String, name: String): PendingServiceAreaAllocation {
    return PendingServiceAreaAllocation(
        code = code,
        name = name,
        depot = "",
        version = 0,
        score = 0.0,
        phone = "",
        status = PendingServiceAreaAllocationStatus.PROPOSED,
    )
}
