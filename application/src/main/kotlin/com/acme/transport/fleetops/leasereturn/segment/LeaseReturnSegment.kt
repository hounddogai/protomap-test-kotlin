package com.acme.transport.fleetops.leasereturn.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lease return segment records of fleetops's lease return work, and the rules, stores, and calculations
// around them.

/** Where a lease return segment is in its life cycle. */
enum class LeaseReturnSegmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a lease return segment and its current status. */
data class LeaseReturnSegment(
    var code: String,
    var label: String,
    var approved: Boolean,
    var budget: Double,
    var visible: Boolean,
    var amount: Double,
    var locale: String,
    var version: Int,
    var status: LeaseReturnSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnSegmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnSegmentStatus): Boolean {
        when (status) {
            LeaseReturnSegmentStatus.CREATED -> return next == LeaseReturnSegmentStatus.VALIDATED || next == LeaseReturnSegmentStatus.PUBLISHED
            LeaseReturnSegmentStatus.VALIDATED -> return next == LeaseReturnSegmentStatus.RETIRED
            LeaseReturnSegmentStatus.PUBLISHED -> return next == LeaseReturnSegmentStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: LeaseReturnSegmentStatus) {
        if (!canMoveTo(next)) {
            error("lease return segment $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveExposure(): Double = round((amount + budget) * 100) / 100

    fun snapshot(): LeaseReturnSegment {
        return LeaseReturnSegment(
            code = code,
            label = label,
            approved = approved,
            budget = budget,
            visible = visible,
            amount = amount,
            locale = locale,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a lease return segment in status created. */
fun newLeaseReturnSegment(code: String, label: String): LeaseReturnSegment {
    return LeaseReturnSegment(
        code = code,
        label = label,
        approved = false,
        budget = 0.0,
        visible = false,
        amount = 0.0,
        locale = "",
        version = 0,
        status = LeaseReturnSegmentStatus.CREATED,
    )
}
