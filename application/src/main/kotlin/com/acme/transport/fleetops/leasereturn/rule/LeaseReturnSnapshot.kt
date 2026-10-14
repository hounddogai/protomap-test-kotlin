package com.acme.transport.fleetops.leasereturn.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lease return snapshot is in its life cycle. */
enum class LeaseReturnSnapshotStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A lease return snapshot. Its code is unique within a tenant. */
data class LeaseReturnSnapshot(
    var code: String,
    var label: String,
    var maximum: Double,
    var priority: Int,
    var durationMinutes: Int,
    var notes: String,
    var contactEmail: String,
    var limit: Int,
    var cost: Double,
    var status: LeaseReturnSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnSnapshotStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnSnapshotStatus): Boolean {
        when (status) {
            LeaseReturnSnapshotStatus.OPEN -> return next == LeaseReturnSnapshotStatus.RESOLVED
            LeaseReturnSnapshotStatus.IN_REVIEW -> {
                when (next) {
                    LeaseReturnSnapshotStatus.RESOLVED -> return true
                }
                return false
            }
            LeaseReturnSnapshotStatus.RESOLVED -> {
                when (next) {
                    LeaseReturnSnapshotStatus.REOPENED, LeaseReturnSnapshotStatus.ARCHIVED -> return true
                }
                return false
            }
            LeaseReturnSnapshotStatus.REOPENED -> {
                when (next) {
                    LeaseReturnSnapshotStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LeaseReturnSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("lease return snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveTotal(): Double = round(cost * priority.toDouble() * 100) / 100

    fun snapshot(): LeaseReturnSnapshot {
        return LeaseReturnSnapshot(
            code = code,
            label = label,
            maximum = maximum,
            priority = priority,
            durationMinutes = durationMinutes,
            notes = notes,
            contactEmail = contactEmail,
            limit = limit,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lease return snapshot in status open. */
fun newLeaseReturnSnapshot(code: String, label: String): LeaseReturnSnapshot {
    return LeaseReturnSnapshot(
        code = code,
        label = label,
        maximum = 0.0,
        priority = 0,
        durationMinutes = 0,
        notes = "",
        contactEmail = "",
        limit = 0,
        cost = 0.0,
        status = LeaseReturnSnapshotStatus.OPEN,
    )
}
