package com.acme.transport.emissions.routeoffset.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The route offset assignment records of emissions's route offset work, and the rules, stores, and
// calculations around them.

/** Where a route offset assignment is in its life cycle. */
enum class RouteOffsetAssignmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A route offset assignment. Its code is unique within a tenant. */
data class RouteOffsetAssignment(
    var code: String,
    var label: String,
    var routeCode: String,
    var vehicleId: String,
    var maximum: Double,
    var retries: Int,
    var updatedAt: Int,
    var visible: Boolean,
    var distanceKm: Double,
    var shiftCode: String,
    var status: RouteOffsetAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetAssignmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetAssignmentStatus): Boolean {
        when (status) {
            RouteOffsetAssignmentStatus.OPEN -> return next == RouteOffsetAssignmentStatus.IN_REVIEW || next == RouteOffsetAssignmentStatus.RESOLVED
            RouteOffsetAssignmentStatus.IN_REVIEW -> return next == RouteOffsetAssignmentStatus.RESOLVED || next == RouteOffsetAssignmentStatus.REOPENED
            RouteOffsetAssignmentStatus.RESOLVED -> {
                when (next) {
                    RouteOffsetAssignmentStatus.REOPENED -> return true
                }
                return false
            }
            RouteOffsetAssignmentStatus.REOPENED -> {
                when (next) {
                    RouteOffsetAssignmentStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RouteOffsetAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("route offset assignment $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"route offset assignment"} $code"

    fun effectiveLoad(): Double = round(distanceKm * updatedAt.toDouble() * 100) / 100

    fun snapshot(): RouteOffsetAssignment {
        return RouteOffsetAssignment(
            code = code,
            label = label,
            routeCode = routeCode,
            vehicleId = vehicleId,
            maximum = maximum,
            retries = retries,
            updatedAt = updatedAt,
            visible = visible,
            distanceKm = distanceKm,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a route offset assignment in status open. */
fun newRouteOffsetAssignment(code: String, label: String): RouteOffsetAssignment {
    return RouteOffsetAssignment(
        code = code,
        label = label,
        routeCode = "",
        vehicleId = "",
        maximum = 0.0,
        retries = 0,
        updatedAt = 0,
        visible = false,
        distanceKm = 0.0,
        shiftCode = "",
        status = RouteOffsetAssignmentStatus.OPEN,
    )
}
