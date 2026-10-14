package com.acme.transport.fleetops.leasereturn.returntier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling lease return tier records of fleetops's lease return work, and the rules, stores, and
// calculations around them.

/** Where a rolling lease return tier is in its life cycle. */
enum class RollingLeaseReturnTierStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a rolling lease return tier and its current status. */
data class RollingLeaseReturnTier(
    var code: String,
    var name: String,
    var endsAt: Int,
    var sequence: Int,
    var score: Double,
    var amount: Double,
    var threshold: Double,
    var margin: Double,
    var pallets: Int,
    var status: RollingLeaseReturnTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingLeaseReturnTierStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingLeaseReturnTierStatus): Boolean {
        when (status) {
            RollingLeaseReturnTierStatus.PLANNED -> {
                when (next) {
                    RollingLeaseReturnTierStatus.SCHEDULED, RollingLeaseReturnTierStatus.IN_PROGRESS -> return true
                }
                return false
            }
            RollingLeaseReturnTierStatus.SCHEDULED -> return next == RollingLeaseReturnTierStatus.COMPLETED || next == RollingLeaseReturnTierStatus.CANCELLED
            RollingLeaseReturnTierStatus.IN_PROGRESS -> {
                when (next) {
                    RollingLeaseReturnTierStatus.COMPLETED, RollingLeaseReturnTierStatus.CANCELLED -> return true
                }
                return false
            }
            RollingLeaseReturnTierStatus.COMPLETED -> return next == RollingLeaseReturnTierStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: RollingLeaseReturnTierStatus) {
        if (!canMoveTo(next)) {
            error("rolling lease return tier $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun weightedVolume(): Double = round((amount + threshold) * 100) / 100

    fun snapshot(): RollingLeaseReturnTier {
        return RollingLeaseReturnTier(
            code = code,
            name = name,
            endsAt = endsAt,
            sequence = sequence,
            score = score,
            amount = amount,
            threshold = threshold,
            margin = margin,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rolling lease return tier in status planned. */
fun newRollingLeaseReturnTier(code: String, name: String): RollingLeaseReturnTier {
    return RollingLeaseReturnTier(
        code = code,
        name = name,
        endsAt = 0,
        sequence = 0,
        score = 0.0,
        amount = 0.0,
        threshold = 0.0,
        margin = 0.0,
        pallets = 0,
        status = RollingLeaseReturnTierStatus.PLANNED,
    )
}
