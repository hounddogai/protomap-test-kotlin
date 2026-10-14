package com.acme.transport.crewing.availability.availabilitytier

import com.acme.transport.kit.text.normalize

// The pending availability tier records of crewing's availability work, and the rules, stores, and
// calculations around them.

/** Where a pending availability tier is in its life cycle. */
enum class PendingAvailabilityTierStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A pending availability tier. Its code is unique within a tenant. */
data class PendingAvailabilityTier(
    var code: String,
    var label: String,
    var approved: Boolean,
    var rate: Double,
    var ownerId: String,
    var pinned: Boolean,
    var description: String,
    var region: String,
    var visible: Boolean,
    var status: PendingAvailabilityTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingAvailabilityTierStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingAvailabilityTierStatus): Boolean {
        when (status) {
            PendingAvailabilityTierStatus.OPEN -> {
                when (next) {
                    PendingAvailabilityTierStatus.RESOLVED -> return true
                }
                return false
            }
            PendingAvailabilityTierStatus.IN_REVIEW -> {
                when (next) {
                    PendingAvailabilityTierStatus.RESOLVED -> return true
                }
                return false
            }
            PendingAvailabilityTierStatus.RESOLVED -> return next == PendingAvailabilityTierStatus.ARCHIVED
            PendingAvailabilityTierStatus.REOPENED -> return next == PendingAvailabilityTierStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: PendingAvailabilityTierStatus) {
        if (!canMoveTo(next)) {
            error("pending availability tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"pending availability tier"} $code"

    fun measuredValue(): Double = rate

    fun snapshot(): PendingAvailabilityTier {
        return PendingAvailabilityTier(
            code = code,
            label = label,
            approved = approved,
            rate = rate,
            ownerId = ownerId,
            pinned = pinned,
            description = description,
            region = region,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a pending availability tier in status open. */
fun newPendingAvailabilityTier(code: String, label: String): PendingAvailabilityTier {
    return PendingAvailabilityTier(
        code = code,
        label = label,
        approved = false,
        rate = 0.0,
        ownerId = "",
        pinned = false,
        description = "",
        region = "",
        visible = false,
        status = PendingAvailabilityTierStatus.OPEN,
    )
}
