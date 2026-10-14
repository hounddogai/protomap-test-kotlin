package com.acme.transport.fleetops.leasereturn.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lease return assignment is in its life cycle. */
enum class LeaseReturnAssignmentStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A lease return assignment. Its code is unique within a tenant. */
data class LeaseReturnAssignment(
    var code: String,
    var label: String,
    var limit: Int,
    var dock: String,
    var currency: String,
    var createdAt: Int,
    var depot: String,
    var cost: Double,
    var pallets: Int,
    var carrier: String,
    var status: LeaseReturnAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnAssignmentStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnAssignmentStatus): Boolean {
        when (status) {
            LeaseReturnAssignmentStatus.DRAFT -> {
                when (next) {
                    LeaseReturnAssignmentStatus.ACTIVE, LeaseReturnAssignmentStatus.SUSPENDED -> return true
                }
                return false
            }
            LeaseReturnAssignmentStatus.PENDING -> {
                when (next) {
                    LeaseReturnAssignmentStatus.ACTIVE -> return true
                }
                return false
            }
            LeaseReturnAssignmentStatus.ACTIVE -> {
                when (next) {
                    LeaseReturnAssignmentStatus.SUSPENDED, LeaseReturnAssignmentStatus.CLOSED -> return true
                }
                return false
            }
            LeaseReturnAssignmentStatus.SUSPENDED -> {
                when (next) {
                    LeaseReturnAssignmentStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LeaseReturnAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("lease return assignment $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedExposure(): Double = round((limit.toDouble() + cost) * 100) / 100

    fun snapshot(): LeaseReturnAssignment {
        return LeaseReturnAssignment(
            code = code,
            label = label,
            limit = limit,
            dock = dock,
            currency = currency,
            createdAt = createdAt,
            depot = depot,
            cost = cost,
            pallets = pallets,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a lease return assignment in status draft. */
fun newLeaseReturnAssignment(code: String, label: String): LeaseReturnAssignment {
    return LeaseReturnAssignment(
        code = code,
        label = label,
        limit = 0,
        dock = "",
        currency = "",
        createdAt = 0,
        depot = "",
        cost = 0.0,
        pallets = 0,
        carrier = "",
        status = LeaseReturnAssignmentStatus.DRAFT,
    )
}
