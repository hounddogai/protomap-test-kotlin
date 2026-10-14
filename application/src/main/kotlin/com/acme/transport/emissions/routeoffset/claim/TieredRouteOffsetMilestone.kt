package com.acme.transport.emissions.routeoffset.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered route offset milestone is in its life cycle. */
enum class TieredRouteOffsetMilestoneStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a tiered route offset milestone and its current status. */
data class TieredRouteOffsetMilestone(
    var code: String,
    var title: String,
    var createdAt: Int,
    var unit: String,
    var currency: String,
    var discount: Double,
    var amount: Double,
    var status: TieredRouteOffsetMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredRouteOffsetMilestoneStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredRouteOffsetMilestoneStatus): Boolean {
        when (status) {
            TieredRouteOffsetMilestoneStatus.PROPOSED -> {
                when (next) {
                    TieredRouteOffsetMilestoneStatus.APPROVED -> return true
                }
                return false
            }
            TieredRouteOffsetMilestoneStatus.APPROVED -> return next == TieredRouteOffsetMilestoneStatus.REJECTED
            TieredRouteOffsetMilestoneStatus.REJECTED -> return next == TieredRouteOffsetMilestoneStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: TieredRouteOffsetMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("tiered route offset milestone $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"tiered route offset milestone"} $code"

    fun netLoad(): Double = round(createdAt.toDouble() * discount * 100) / 100

    fun snapshot(): TieredRouteOffsetMilestone {
        return TieredRouteOffsetMilestone(
            code = code,
            title = title,
            createdAt = createdAt,
            unit = unit,
            currency = currency,
            discount = discount,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a tiered route offset milestone in status proposed. */
fun newTieredRouteOffsetMilestone(code: String, title: String): TieredRouteOffsetMilestone {
    return TieredRouteOffsetMilestone(
        code = code,
        title = title,
        createdAt = 0,
        unit = "",
        currency = "",
        discount = 0.0,
        amount = 0.0,
        status = TieredRouteOffsetMilestoneStatus.PROPOSED,
    )
}
