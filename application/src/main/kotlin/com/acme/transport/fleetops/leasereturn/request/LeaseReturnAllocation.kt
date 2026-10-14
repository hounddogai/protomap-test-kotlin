package com.acme.transport.fleetops.leasereturn.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lease return allocation is in its life cycle. */
enum class LeaseReturnAllocationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A lease return allocation as the lease return team keeps it. */
data class LeaseReturnAllocation(
    var code: String,
    var title: String,
    var loadKg: Double,
    var category: String,
    var name: String,
    var priority: Int,
    var region: String,
    var externalRef: String,
    var createdAt: Int,
    var endsAt: Int,
    var locale: String,
    var status: LeaseReturnAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnAllocationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnAllocationStatus): Boolean {
        when (status) {
            LeaseReturnAllocationStatus.DRAFT -> {
                when (next) {
                    LeaseReturnAllocationStatus.SUSPENDED -> return true
                }
                return false
            }
            LeaseReturnAllocationStatus.PENDING -> {
                when (next) {
                    LeaseReturnAllocationStatus.ACTIVE, LeaseReturnAllocationStatus.CLOSED -> return true
                }
                return false
            }
            LeaseReturnAllocationStatus.ACTIVE -> {
                when (next) {
                    LeaseReturnAllocationStatus.SUSPENDED -> return true
                }
                return false
            }
            LeaseReturnAllocationStatus.SUSPENDED -> return next == LeaseReturnAllocationStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: LeaseReturnAllocationStatus) {
        if (!canMoveTo(next)) {
            error("lease return allocation $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun projectedLoad(): Double = round((loadKg + endsAt.toDouble()) * 100) / 100

    fun snapshot(): LeaseReturnAllocation {
        return LeaseReturnAllocation(
            code = code,
            title = title,
            loadKg = loadKg,
            category = category,
            name = name,
            priority = priority,
            region = region,
            externalRef = externalRef,
            createdAt = createdAt,
            endsAt = endsAt,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a lease return allocation in status draft. */
fun newLeaseReturnAllocation(code: String, title: String): LeaseReturnAllocation {
    return LeaseReturnAllocation(
        code = code,
        title = title,
        loadKg = 0.0,
        category = "",
        name = "",
        priority = 0,
        region = "",
        externalRef = "",
        createdAt = 0,
        endsAt = 0,
        locale = "",
        status = LeaseReturnAllocationStatus.DRAFT,
    )
}
