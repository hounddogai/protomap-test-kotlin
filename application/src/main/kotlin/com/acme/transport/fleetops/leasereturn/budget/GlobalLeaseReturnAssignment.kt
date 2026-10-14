package com.acme.transport.fleetops.leasereturn.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global lease return assignment is in its life cycle. */
enum class GlobalLeaseReturnAssignmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A global lease return assignment. Its code is unique within a tenant. */
data class GlobalLeaseReturnAssignment(
    var code: String,
    var title: String,
    var durationMinutes: Int,
    var carrier: String,
    var ratio: Double,
    var startsAt: Int,
    var channel: String,
    var limit: Int,
    var maximum: Double,
    var discount: Double,
    var depot: String,
    var status: GlobalLeaseReturnAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalLeaseReturnAssignmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalLeaseReturnAssignmentStatus): Boolean {
        when (status) {
            GlobalLeaseReturnAssignmentStatus.IDLE -> {
                when (next) {
                    GlobalLeaseReturnAssignmentStatus.WARMING, GlobalLeaseReturnAssignmentStatus.READY -> return true
                }
                return false
            }
            GlobalLeaseReturnAssignmentStatus.WARMING -> {
                when (next) {
                    GlobalLeaseReturnAssignmentStatus.STOPPED -> return true
                }
                return false
            }
            GlobalLeaseReturnAssignmentStatus.READY -> {
                when (next) {
                    GlobalLeaseReturnAssignmentStatus.DRAINING, GlobalLeaseReturnAssignmentStatus.STOPPED -> return true
                }
                return false
            }
            GlobalLeaseReturnAssignmentStatus.DRAINING -> return next == GlobalLeaseReturnAssignmentStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: GlobalLeaseReturnAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("global lease return assignment $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"global lease return assignment"} $code"

    fun adjustedTotal(): Double = round((limit.toDouble() + discount) * 100) / 100

    fun snapshot(): GlobalLeaseReturnAssignment {
        return GlobalLeaseReturnAssignment(
            code = code,
            title = title,
            durationMinutes = durationMinutes,
            carrier = carrier,
            ratio = ratio,
            startsAt = startsAt,
            channel = channel,
            limit = limit,
            maximum = maximum,
            discount = discount,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a global lease return assignment in status idle. */
fun newGlobalLeaseReturnAssignment(code: String, title: String): GlobalLeaseReturnAssignment {
    return GlobalLeaseReturnAssignment(
        code = code,
        title = title,
        durationMinutes = 0,
        carrier = "",
        ratio = 0.0,
        startsAt = 0,
        channel = "",
        limit = 0,
        maximum = 0.0,
        discount = 0.0,
        depot = "",
        status = GlobalLeaseReturnAssignmentStatus.IDLE,
    )
}
