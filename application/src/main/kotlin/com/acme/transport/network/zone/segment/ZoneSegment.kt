package com.acme.transport.network.zone.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The zone segment records of network's zone work, and the rules, stores, and calculations around them.

/** Where a zone segment is in its life cycle. */
enum class ZoneSegmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A zone segment as the zone team keeps it. */
data class ZoneSegment(
    var code: String,
    var name: String,
    var segment: String,
    var description: String,
    var vehicleId: String,
    var distanceKm: Double,
    var minimum: Double,
    var routeCode: String,
    var title: String,
    var weight: Double,
    var status: ZoneSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneSegmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneSegmentStatus): Boolean {
        when (status) {
            ZoneSegmentStatus.PLANNED -> {
                when (next) {
                    ZoneSegmentStatus.SCHEDULED -> return true
                }
                return false
            }
            ZoneSegmentStatus.SCHEDULED -> {
                when (next) {
                    ZoneSegmentStatus.COMPLETED, ZoneSegmentStatus.CANCELLED -> return true
                }
                return false
            }
            ZoneSegmentStatus.IN_PROGRESS -> return next == ZoneSegmentStatus.COMPLETED || next == ZoneSegmentStatus.CANCELLED
            ZoneSegmentStatus.COMPLETED -> return next == ZoneSegmentStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: ZoneSegmentStatus) {
        if (!canMoveTo(next)) {
            error("zone segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"zone segment"} $code"

    fun adjustedTotal(): Double = round(distanceKm * weight * 100) / 100

    fun snapshot(): ZoneSegment {
        return ZoneSegment(
            code = code,
            name = name,
            segment = segment,
            description = description,
            vehicleId = vehicleId,
            distanceKm = distanceKm,
            minimum = minimum,
            routeCode = routeCode,
            title = title,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a zone segment in status planned. */
fun newZoneSegment(code: String, name: String): ZoneSegment {
    return ZoneSegment(
        code = code,
        name = name,
        segment = "",
        description = "",
        vehicleId = "",
        distanceKm = 0.0,
        minimum = 0.0,
        routeCode = "",
        title = "",
        weight = 0.0,
        status = ZoneSegmentStatus.PLANNED,
    )
}
