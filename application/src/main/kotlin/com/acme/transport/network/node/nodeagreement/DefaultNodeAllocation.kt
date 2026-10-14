package com.acme.transport.network.node.nodeagreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default node allocation is in its life cycle. */
enum class DefaultNodeAllocationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One default node allocation, with what the planners need to act on it. */
data class DefaultNodeAllocation(
    var code: String,
    var title: String,
    var driverId: String,
    var margin: Double,
    var limit: Int,
    var routeCode: String,
    var pallets: Int,
    var cost: Double,
    var currency: String,
    var active: Boolean,
    var status: DefaultNodeAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultNodeAllocationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultNodeAllocationStatus): Boolean {
        when (status) {
            DefaultNodeAllocationStatus.DRAFT -> {
                when (next) {
                    DefaultNodeAllocationStatus.SUSPENDED -> return true
                }
                return false
            }
            DefaultNodeAllocationStatus.PENDING -> {
                when (next) {
                    DefaultNodeAllocationStatus.ACTIVE, DefaultNodeAllocationStatus.CLOSED -> return true
                }
                return false
            }
            DefaultNodeAllocationStatus.ACTIVE -> return next == DefaultNodeAllocationStatus.SUSPENDED
            DefaultNodeAllocationStatus.SUSPENDED -> return next == DefaultNodeAllocationStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: DefaultNodeAllocationStatus) {
        if (!canMoveTo(next)) {
            error("default node allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun effectiveVolume(): Double = round((cost - pallets.toDouble()) * 100) / 100

    fun snapshot(): DefaultNodeAllocation {
        return DefaultNodeAllocation(
            code = code,
            title = title,
            driverId = driverId,
            margin = margin,
            limit = limit,
            routeCode = routeCode,
            pallets = pallets,
            cost = cost,
            currency = currency,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a default node allocation in status draft. */
fun newDefaultNodeAllocation(code: String, title: String): DefaultNodeAllocation {
    return DefaultNodeAllocation(
        code = code,
        title = title,
        driverId = "",
        margin = 0.0,
        limit = 0,
        routeCode = "",
        pallets = 0,
        cost = 0.0,
        currency = "",
        active = false,
        status = DefaultNodeAllocationStatus.DRAFT,
    )
}
