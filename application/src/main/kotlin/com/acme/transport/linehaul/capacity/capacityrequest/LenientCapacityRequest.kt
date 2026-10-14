package com.acme.transport.linehaul.capacity.capacityrequest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient capacity request records of linehaul's capacity work, and the rules, stores, and calculations
// around them.

/** Where a lenient capacity request is in its life cycle. */
enum class LenientCapacityRequestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A lenient capacity request. Its code is unique within a tenant. */
data class LenientCapacityRequest(
    var code: String,
    var title: String,
    var floorValue: Double,
    var distanceKm: Double,
    var shiftCode: String,
    var status: LenientCapacityRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientCapacityRequestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientCapacityRequestStatus): Boolean {
        when (status) {
            LenientCapacityRequestStatus.REQUESTED -> {
                when (next) {
                    LenientCapacityRequestStatus.GRANTED, LenientCapacityRequestStatus.REVOKED -> return true
                }
                return false
            }
            LenientCapacityRequestStatus.GRANTED -> {
                when (next) {
                    LenientCapacityRequestStatus.REVOKED, LenientCapacityRequestStatus.EXPIRED -> return true
                }
                return false
            }
            LenientCapacityRequestStatus.REVOKED -> return next == LenientCapacityRequestStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: LenientCapacityRequestStatus) {
        if (!canMoveTo(next)) {
            error("lenient capacity request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"lenient capacity request"} $code"

    fun effectiveLoad(): Double = round((floorValue - distanceKm) * 100) / 100

    fun snapshot(): LenientCapacityRequest {
        return LenientCapacityRequest(
            code = code,
            title = title,
            floorValue = floorValue,
            distanceKm = distanceKm,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lenient capacity request in status requested. */
fun newLenientCapacityRequest(code: String, title: String): LenientCapacityRequest {
    return LenientCapacityRequest(
        code = code,
        title = title,
        floorValue = 0.0,
        distanceKm = 0.0,
        shiftCode = "",
        status = LenientCapacityRequestStatus.REQUESTED,
    )
}
