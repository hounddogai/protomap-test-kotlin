package com.acme.transport.emissions.routeoffset.offsetassignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a route offset budget is in its life cycle. */
enum class RouteOffsetBudgetStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A route offset budget as the route offset team keeps it. */
data class RouteOffsetBudget(
    var code: String,
    var label: String,
    var floorValue: Double,
    var sequence: Int,
    var depot: String,
    var odometer: Int,
    var timezone: String,
    var capacity: Int,
    var country: String,
    var retries: Int,
    var status: RouteOffsetBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetBudgetStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetBudgetStatus): Boolean {
        when (status) {
            RouteOffsetBudgetStatus.NEW -> {
                when (next) {
                    RouteOffsetBudgetStatus.QUEUED -> return true
                }
                return false
            }
            RouteOffsetBudgetStatus.QUEUED -> {
                when (next) {
                    RouteOffsetBudgetStatus.RUNNING, RouteOffsetBudgetStatus.FAILED -> return true
                }
                return false
            }
            RouteOffsetBudgetStatus.RUNNING -> return next == RouteOffsetBudgetStatus.DONE
            RouteOffsetBudgetStatus.DONE -> return next == RouteOffsetBudgetStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RouteOffsetBudgetStatus) {
        if (!canMoveTo(next)) {
            error("route offset budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedTotal(): Double = round((capacity.toDouble() - retries.toDouble()) * 100) / 100

    fun snapshot(): RouteOffsetBudget {
        return RouteOffsetBudget(
            code = code,
            label = label,
            floorValue = floorValue,
            sequence = sequence,
            depot = depot,
            odometer = odometer,
            timezone = timezone,
            capacity = capacity,
            country = country,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a route offset budget in status new. */
fun newRouteOffsetBudget(code: String, label: String): RouteOffsetBudget {
    return RouteOffsetBudget(
        code = code,
        label = label,
        floorValue = 0.0,
        sequence = 0,
        depot = "",
        odometer = 0,
        timezone = "",
        capacity = 0,
        country = "",
        retries = 0,
        status = RouteOffsetBudgetStatus.NEW,
    )
}
