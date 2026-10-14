package com.acme.transport.fleetops.leasereturn.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lease return tier records of fleetops's lease return work, and the rules, stores, and calculations
// around them.

/** Where a lease return tier is in its life cycle. */
enum class LeaseReturnTierStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A lease return tier. Its code is unique within a tenant. */
data class LeaseReturnTier(
    var code: String,
    var label: String,
    var lane: String,
    var archived: Boolean,
    var segment: String,
    var amount: Double,
    var quantity: Int,
    var locale: String,
    var status: LeaseReturnTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnTierStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnTierStatus): Boolean {
        when (status) {
            LeaseReturnTierStatus.PLANNED -> {
                when (next) {
                    LeaseReturnTierStatus.CANCELLED -> return true
                }
                return false
            }
            LeaseReturnTierStatus.SCHEDULED -> return next == LeaseReturnTierStatus.IN_PROGRESS
            LeaseReturnTierStatus.IN_PROGRESS -> return next == LeaseReturnTierStatus.COMPLETED
            LeaseReturnTierStatus.COMPLETED -> return next == LeaseReturnTierStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LeaseReturnTierStatus) {
        if (!canMoveTo(next)) {
            error("lease return tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveExposure(): Double = round((amount - quantity.toDouble()) * 100) / 100

    fun snapshot(): LeaseReturnTier {
        return LeaseReturnTier(
            code = code,
            label = label,
            lane = lane,
            archived = archived,
            segment = segment,
            amount = amount,
            quantity = quantity,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a lease return tier in status planned. */
fun newLeaseReturnTier(code: String, label: String): LeaseReturnTier {
    return LeaseReturnTier(
        code = code,
        label = label,
        lane = "",
        archived = false,
        segment = "",
        amount = 0.0,
        quantity = 0,
        locale = "",
        status = LeaseReturnTierStatus.PLANNED,
    )
}
