package com.acme.transport.network.zone.zonemapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a zone assignment is in its life cycle. */
enum class ZoneAssignmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One zone assignment, with what the planners need to act on it. */
data class ZoneAssignment(
    var code: String,
    var label: String,
    var quantity: Int,
    var distanceKm: Double,
    var ceiling: Double,
    var floorValue: Double,
    var threshold: Double,
    var depot: String,
    var fuelLiters: Double,
    var discount: Double,
    var score: Double,
    var status: ZoneAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneAssignmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneAssignmentStatus): Boolean {
        when (status) {
            ZoneAssignmentStatus.CREATED -> return next == ZoneAssignmentStatus.VALIDATED || next == ZoneAssignmentStatus.RETIRED
            ZoneAssignmentStatus.VALIDATED -> return next == ZoneAssignmentStatus.RETIRED
            ZoneAssignmentStatus.PUBLISHED -> return next == ZoneAssignmentStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ZoneAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("zone assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedVolume(): Double = round((score + discount) * 100) / 100

    fun snapshot(): ZoneAssignment {
        return ZoneAssignment(
            code = code,
            label = label,
            quantity = quantity,
            distanceKm = distanceKm,
            ceiling = ceiling,
            floorValue = floorValue,
            threshold = threshold,
            depot = depot,
            fuelLiters = fuelLiters,
            discount = discount,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a zone assignment in status created. */
fun newZoneAssignment(code: String, label: String): ZoneAssignment {
    return ZoneAssignment(
        code = code,
        label = label,
        quantity = 0,
        distanceKm = 0.0,
        ceiling = 0.0,
        floorValue = 0.0,
        threshold = 0.0,
        depot = "",
        fuelLiters = 0.0,
        discount = 0.0,
        score = 0.0,
        status = ZoneAssignmentStatus.CREATED,
    )
}
