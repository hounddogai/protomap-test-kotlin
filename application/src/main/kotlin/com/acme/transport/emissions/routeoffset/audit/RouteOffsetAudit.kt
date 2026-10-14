package com.acme.transport.emissions.routeoffset.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The route offset audit records of emissions's route offset work, and the rules, stores, and calculations
// around them.

/** Where a route offset audit is in its life cycle. */
enum class RouteOffsetAuditStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a route offset audit and its current status. */
data class RouteOffsetAudit(
    var code: String,
    var label: String,
    var odometer: Int,
    var retries: Int,
    var capacity: Int,
    var trailerId: String,
    var quantity: Int,
    var status: RouteOffsetAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetAuditStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetAuditStatus): Boolean {
        when (status) {
            RouteOffsetAuditStatus.PLANNED -> {
                when (next) {
                    RouteOffsetAuditStatus.SCHEDULED, RouteOffsetAuditStatus.IN_PROGRESS -> return true
                }
                return false
            }
            RouteOffsetAuditStatus.SCHEDULED -> return next == RouteOffsetAuditStatus.CANCELLED
            RouteOffsetAuditStatus.IN_PROGRESS -> return next == RouteOffsetAuditStatus.CANCELLED
            RouteOffsetAuditStatus.COMPLETED -> {
                when (next) {
                    RouteOffsetAuditStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RouteOffsetAuditStatus) {
        if (!canMoveTo(next)) {
            error("route offset audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"route offset audit"} $code"

    fun projectedTotal(): Double = round((quantity.toDouble() + odometer.toDouble()) * 100) / 100

    fun snapshot(): RouteOffsetAudit {
        return RouteOffsetAudit(
            code = code,
            label = label,
            odometer = odometer,
            retries = retries,
            capacity = capacity,
            trailerId = trailerId,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a route offset audit in status planned. */
fun newRouteOffsetAudit(code: String, label: String): RouteOffsetAudit {
    return RouteOffsetAudit(
        code = code,
        label = label,
        odometer = 0,
        retries = 0,
        capacity = 0,
        trailerId = "",
        quantity = 0,
        status = RouteOffsetAuditStatus.PLANNED,
    )
}
