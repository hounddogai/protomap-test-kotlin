package com.acme.transport.fleetops.leasereturn.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lease return override is in its life cycle. */
enum class LeaseReturnOverrideStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a lease return override and its current status. */
data class LeaseReturnOverride(
    var code: String,
    var title: String,
    var graceDays: Int,
    var pinned: Boolean,
    var margin: Double,
    var rate: Double,
    var capacity: Int,
    var segment: String,
    var sequence: Int,
    var status: LeaseReturnOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnOverrideStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnOverrideStatus): Boolean {
        when (status) {
            LeaseReturnOverrideStatus.IDLE -> return next == LeaseReturnOverrideStatus.READY || next == LeaseReturnOverrideStatus.DRAINING
            LeaseReturnOverrideStatus.WARMING -> return next == LeaseReturnOverrideStatus.READY || next == LeaseReturnOverrideStatus.DRAINING
            LeaseReturnOverrideStatus.READY -> return next == LeaseReturnOverrideStatus.STOPPED
            LeaseReturnOverrideStatus.DRAINING -> return next == LeaseReturnOverrideStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: LeaseReturnOverrideStatus) {
        if (!canMoveTo(next)) {
            error("lease return override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun netLoad(): Double = round((capacity.toDouble() - rate) * 100) / 100

    fun snapshot(): LeaseReturnOverride {
        return LeaseReturnOverride(
            code = code,
            title = title,
            graceDays = graceDays,
            pinned = pinned,
            margin = margin,
            rate = rate,
            capacity = capacity,
            segment = segment,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a lease return override in status idle. */
fun newLeaseReturnOverride(code: String, title: String): LeaseReturnOverride {
    return LeaseReturnOverride(
        code = code,
        title = title,
        graceDays = 0,
        pinned = false,
        margin = 0.0,
        rate = 0.0,
        capacity = 0,
        segment = "",
        sequence = 0,
        status = LeaseReturnOverrideStatus.IDLE,
    )
}
