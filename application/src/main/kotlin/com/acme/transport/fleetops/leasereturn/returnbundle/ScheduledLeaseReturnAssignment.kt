package com.acme.transport.fleetops.leasereturn.returnbundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled lease return assignment is in its life cycle. */
enum class ScheduledLeaseReturnAssignmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A scheduled lease return assignment. Its code is unique within a tenant. */
data class ScheduledLeaseReturnAssignment(
    var code: String,
    var name: String,
    var tags: MutableList<String>,
    var depot: String,
    var currency: String,
    var reviewer: String,
    var retries: Int,
    var startsAt: Int,
    var visible: Boolean,
    var status: ScheduledLeaseReturnAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledLeaseReturnAssignmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledLeaseReturnAssignmentStatus): Boolean {
        when (status) {
            ScheduledLeaseReturnAssignmentStatus.IDLE -> {
                when (next) {
                    ScheduledLeaseReturnAssignmentStatus.DRAINING -> return true
                }
                return false
            }
            ScheduledLeaseReturnAssignmentStatus.WARMING -> {
                when (next) {
                    ScheduledLeaseReturnAssignmentStatus.DRAINING -> return true
                }
                return false
            }
            ScheduledLeaseReturnAssignmentStatus.READY -> {
                when (next) {
                    ScheduledLeaseReturnAssignmentStatus.DRAINING, ScheduledLeaseReturnAssignmentStatus.STOPPED -> return true
                }
                return false
            }
            ScheduledLeaseReturnAssignmentStatus.DRAINING -> {
                when (next) {
                    ScheduledLeaseReturnAssignmentStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledLeaseReturnAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("scheduled lease return assignment $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveLoad(): Double = round((startsAt.toDouble() + retries.toDouble()) * 100) / 100

    fun snapshot(): ScheduledLeaseReturnAssignment {
        return ScheduledLeaseReturnAssignment(
            code = code,
            name = name,
            tags = tags,
            depot = depot,
            currency = currency,
            reviewer = reviewer,
            retries = retries,
            startsAt = startsAt,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a scheduled lease return assignment in status idle. */
fun newScheduledLeaseReturnAssignment(code: String, name: String): ScheduledLeaseReturnAssignment {
    return ScheduledLeaseReturnAssignment(
        code = code,
        name = name,
        tags = mutableListOf<String>(),
        depot = "",
        currency = "",
        reviewer = "",
        retries = 0,
        startsAt = 0,
        visible = false,
        status = ScheduledLeaseReturnAssignmentStatus.IDLE,
    )
}
