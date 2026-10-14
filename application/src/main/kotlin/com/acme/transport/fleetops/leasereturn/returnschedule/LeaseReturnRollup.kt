package com.acme.transport.fleetops.leasereturn.returnschedule

import com.acme.transport.kit.text.normalize

/** Where a lease return rollup is in its life cycle. */
enum class LeaseReturnRollupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One lease return rollup, with what the planners need to act on it. */
data class LeaseReturnRollup(
    var code: String,
    var name: String,
    var carrier: String,
    var title: String,
    var batchSize: Int,
    var status: LeaseReturnRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnRollupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnRollupStatus): Boolean {
        when (status) {
            LeaseReturnRollupStatus.PLANNED -> {
                when (next) {
                    LeaseReturnRollupStatus.IN_PROGRESS, LeaseReturnRollupStatus.COMPLETED -> return true
                }
                return false
            }
            LeaseReturnRollupStatus.SCHEDULED -> return next == LeaseReturnRollupStatus.CANCELLED
            LeaseReturnRollupStatus.IN_PROGRESS -> {
                when (next) {
                    LeaseReturnRollupStatus.COMPLETED, LeaseReturnRollupStatus.CANCELLED -> return true
                }
                return false
            }
            LeaseReturnRollupStatus.COMPLETED -> return next == LeaseReturnRollupStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LeaseReturnRollupStatus) {
        if (!canMoveTo(next)) {
            error("lease return rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = batchSize.toDouble()

    fun snapshot(): LeaseReturnRollup {
        return LeaseReturnRollup(
            code = code,
            name = name,
            carrier = carrier,
            title = title,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lease return rollup in status planned. */
fun newLeaseReturnRollup(code: String, name: String): LeaseReturnRollup {
    return LeaseReturnRollup(
        code = code,
        name = name,
        carrier = "",
        title = "",
        batchSize = 0,
        status = LeaseReturnRollupStatus.PLANNED,
    )
}
