package com.acme.transport.fleetops.leasereturn.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict lease return baseline records of fleetops's lease return work, and the rules, stores, and
// calculations around them.

/** Where a strict lease return baseline is in its life cycle. */
enum class StrictLeaseReturnBaselineStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A strict lease return baseline. Its code is unique within a tenant. */
data class StrictLeaseReturnBaseline(
    var code: String,
    var label: String,
    var category: String,
    var segment: String,
    var minimum: Double,
    var pallets: Int,
    var maximum: Double,
    var startsAt: Int,
    var status: StrictLeaseReturnBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictLeaseReturnBaselineStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictLeaseReturnBaselineStatus): Boolean {
        when (status) {
            StrictLeaseReturnBaselineStatus.PROPOSED -> {
                when (next) {
                    StrictLeaseReturnBaselineStatus.APPROVED -> return true
                }
                return false
            }
            StrictLeaseReturnBaselineStatus.APPROVED -> {
                when (next) {
                    StrictLeaseReturnBaselineStatus.REJECTED, StrictLeaseReturnBaselineStatus.WITHDRAWN -> return true
                }
                return false
            }
            StrictLeaseReturnBaselineStatus.REJECTED -> return next == StrictLeaseReturnBaselineStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: StrictLeaseReturnBaselineStatus) {
        if (!canMoveTo(next)) {
            error("strict lease return baseline $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"strict lease return baseline"} $code"

    fun effectiveLoad(): Double = round(startsAt.toDouble() * pallets.toDouble() * 100) / 100

    fun snapshot(): StrictLeaseReturnBaseline {
        return StrictLeaseReturnBaseline(
            code = code,
            label = label,
            category = category,
            segment = segment,
            minimum = minimum,
            pallets = pallets,
            maximum = maximum,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a strict lease return baseline in status proposed. */
fun newStrictLeaseReturnBaseline(code: String, label: String): StrictLeaseReturnBaseline {
    return StrictLeaseReturnBaseline(
        code = code,
        label = label,
        category = "",
        segment = "",
        minimum = 0.0,
        pallets = 0,
        maximum = 0.0,
        startsAt = 0,
        status = StrictLeaseReturnBaselineStatus.PROPOSED,
    )
}
