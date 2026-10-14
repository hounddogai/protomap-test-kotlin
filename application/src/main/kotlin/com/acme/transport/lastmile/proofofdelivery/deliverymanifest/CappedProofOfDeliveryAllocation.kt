package com.acme.transport.lastmile.proofofdelivery.deliverymanifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped proof of delivery allocation is in its life cycle. */
enum class CappedProofOfDeliveryAllocationStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One capped proof of delivery allocation, with what the planners need to act on it. */
data class CappedProofOfDeliveryAllocation(
    var code: String,
    var title: String,
    var channel: String,
    var startsAt: Int,
    var windowDays: Int,
    var status: CappedProofOfDeliveryAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedProofOfDeliveryAllocationStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedProofOfDeliveryAllocationStatus): Boolean {
        when (status) {
            CappedProofOfDeliveryAllocationStatus.OPEN -> return next == CappedProofOfDeliveryAllocationStatus.IN_REVIEW || next == CappedProofOfDeliveryAllocationStatus.REOPENED
            CappedProofOfDeliveryAllocationStatus.IN_REVIEW -> return next == CappedProofOfDeliveryAllocationStatus.RESOLVED
            CappedProofOfDeliveryAllocationStatus.RESOLVED -> return next == CappedProofOfDeliveryAllocationStatus.REOPENED || next == CappedProofOfDeliveryAllocationStatus.ARCHIVED
            CappedProofOfDeliveryAllocationStatus.REOPENED -> return next == CappedProofOfDeliveryAllocationStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: CappedProofOfDeliveryAllocationStatus) {
        if (!canMoveTo(next)) {
            error("capped proof of delivery allocation $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun effectiveLoad(): Double = round((windowDays.toDouble() - startsAt.toDouble()) * 100) / 100

    fun snapshot(): CappedProofOfDeliveryAllocation {
        return CappedProofOfDeliveryAllocation(
            code = code,
            title = title,
            channel = channel,
            startsAt = startsAt,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a capped proof of delivery allocation in status open. */
fun newCappedProofOfDeliveryAllocation(code: String, title: String): CappedProofOfDeliveryAllocation {
    return CappedProofOfDeliveryAllocation(
        code = code,
        title = title,
        channel = "",
        startsAt = 0,
        windowDays = 0,
        status = CappedProofOfDeliveryAllocationStatus.OPEN,
    )
}
