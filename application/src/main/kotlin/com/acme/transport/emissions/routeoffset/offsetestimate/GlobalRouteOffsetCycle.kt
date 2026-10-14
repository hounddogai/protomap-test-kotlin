package com.acme.transport.emissions.routeoffset.offsetestimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global route offset cycle is in its life cycle. */
enum class GlobalRouteOffsetCycleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One global route offset cycle, with what the planners need to act on it. */
data class GlobalRouteOffsetCycle(
    var code: String,
    var name: String,
    var quantity: Int,
    var budget: Double,
    var pallets: Int,
    var channel: String,
    var cost: Double,
    var region: String,
    var category: String,
    var status: GlobalRouteOffsetCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalRouteOffsetCycleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalRouteOffsetCycleStatus): Boolean {
        when (status) {
            GlobalRouteOffsetCycleStatus.DRAFT -> {
                when (next) {
                    GlobalRouteOffsetCycleStatus.PENDING, GlobalRouteOffsetCycleStatus.SUSPENDED -> return true
                }
                return false
            }
            GlobalRouteOffsetCycleStatus.PENDING -> {
                when (next) {
                    GlobalRouteOffsetCycleStatus.CLOSED -> return true
                }
                return false
            }
            GlobalRouteOffsetCycleStatus.ACTIVE -> {
                when (next) {
                    GlobalRouteOffsetCycleStatus.SUSPENDED -> return true
                }
                return false
            }
            GlobalRouteOffsetCycleStatus.SUSPENDED -> {
                when (next) {
                    GlobalRouteOffsetCycleStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalRouteOffsetCycleStatus) {
        if (!canMoveTo(next)) {
            error("global route offset cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun adjustedLoad(): Double = round(cost * quantity.toDouble() * 100) / 100

    fun snapshot(): GlobalRouteOffsetCycle {
        return GlobalRouteOffsetCycle(
            code = code,
            name = name,
            quantity = quantity,
            budget = budget,
            pallets = pallets,
            channel = channel,
            cost = cost,
            region = region,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a global route offset cycle in status draft. */
fun newGlobalRouteOffsetCycle(code: String, name: String): GlobalRouteOffsetCycle {
    return GlobalRouteOffsetCycle(
        code = code,
        name = name,
        quantity = 0,
        budget = 0.0,
        pallets = 0,
        channel = "",
        cost = 0.0,
        region = "",
        category = "",
        status = GlobalRouteOffsetCycleStatus.DRAFT,
    )
}
