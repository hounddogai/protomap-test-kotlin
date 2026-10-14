package com.acme.transport.terminal.dockdoor.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback dock door assignment records of terminal's dock door work, and the rules, stores, and
// calculations around them.

/** Where a fallback dock door assignment is in its life cycle. */
enum class FallbackDockDoorAssignmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a fallback dock door assignment and its current status. */
data class FallbackDockDoorAssignment(
    var code: String,
    var name: String,
    var priority: Int,
    var threshold: Double,
    var unit: String,
    var driverId: String,
    var ratio: Double,
    var fuelLiters: Double,
    var vehicleId: String,
    var routeCode: String,
    var status: FallbackDockDoorAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackDockDoorAssignmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackDockDoorAssignmentStatus): Boolean {
        when (status) {
            FallbackDockDoorAssignmentStatus.REQUESTED -> return next == FallbackDockDoorAssignmentStatus.GRANTED || next == FallbackDockDoorAssignmentStatus.EXPIRED
            FallbackDockDoorAssignmentStatus.GRANTED -> {
                when (next) {
                    FallbackDockDoorAssignmentStatus.REVOKED, FallbackDockDoorAssignmentStatus.EXPIRED -> return true
                }
                return false
            }
            FallbackDockDoorAssignmentStatus.REVOKED -> {
                when (next) {
                    FallbackDockDoorAssignmentStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackDockDoorAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("fallback dock door assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"fallback dock door assignment"} $code"

    fun netTotal(): Double = round((ratio + fuelLiters) * 100) / 100

    fun snapshot(): FallbackDockDoorAssignment {
        return FallbackDockDoorAssignment(
            code = code,
            name = name,
            priority = priority,
            threshold = threshold,
            unit = unit,
            driverId = driverId,
            ratio = ratio,
            fuelLiters = fuelLiters,
            vehicleId = vehicleId,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a fallback dock door assignment in status requested. */
fun newFallbackDockDoorAssignment(code: String, name: String): FallbackDockDoorAssignment {
    return FallbackDockDoorAssignment(
        code = code,
        name = name,
        priority = 0,
        threshold = 0.0,
        unit = "",
        driverId = "",
        ratio = 0.0,
        fuelLiters = 0.0,
        vehicleId = "",
        routeCode = "",
        status = FallbackDockDoorAssignmentStatus.REQUESTED,
    )
}
