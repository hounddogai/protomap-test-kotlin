package com.acme.transport.fleetops.leasereturn.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled lease return mapping records of fleetops's lease return work, and the rules, stores, and
// calculations around them.

/** Where a scheduled lease return mapping is in its life cycle. */
enum class ScheduledLeaseReturnMappingStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A scheduled lease return mapping as the lease return team keeps it. */
data class ScheduledLeaseReturnMapping(
    var code: String,
    var name: String,
    var summary: String,
    var amount: Double,
    var label: String,
    var limit: Int,
    var depot: String,
    var active: Boolean,
    var status: ScheduledLeaseReturnMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledLeaseReturnMappingStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledLeaseReturnMappingStatus): Boolean {
        when (status) {
            ScheduledLeaseReturnMappingStatus.OPEN -> return next == ScheduledLeaseReturnMappingStatus.ARCHIVED
            ScheduledLeaseReturnMappingStatus.IN_REVIEW -> return next == ScheduledLeaseReturnMappingStatus.RESOLVED
            ScheduledLeaseReturnMappingStatus.RESOLVED -> {
                when (next) {
                    ScheduledLeaseReturnMappingStatus.ARCHIVED -> return true
                }
                return false
            }
            ScheduledLeaseReturnMappingStatus.REOPENED -> return next == ScheduledLeaseReturnMappingStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ScheduledLeaseReturnMappingStatus) {
        if (!canMoveTo(next)) {
            error("scheduled lease return mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"scheduled lease return mapping"} $code"

    fun effectiveValue(): Double = round(amount * limit.toDouble() * 100) / 100

    fun snapshot(): ScheduledLeaseReturnMapping {
        return ScheduledLeaseReturnMapping(
            code = code,
            name = name,
            summary = summary,
            amount = amount,
            label = label,
            limit = limit,
            depot = depot,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a scheduled lease return mapping in status open. */
fun newScheduledLeaseReturnMapping(code: String, name: String): ScheduledLeaseReturnMapping {
    return ScheduledLeaseReturnMapping(
        code = code,
        name = name,
        summary = "",
        amount = 0.0,
        label = "",
        limit = 0,
        depot = "",
        active = false,
        status = ScheduledLeaseReturnMappingStatus.OPEN,
    )
}
