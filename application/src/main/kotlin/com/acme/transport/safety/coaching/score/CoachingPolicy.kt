package com.acme.transport.safety.coaching.score

import com.acme.transport.kit.text.normalize

/** Where a coaching policy is in its life cycle. */
enum class CoachingPolicyStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A coaching policy. Its code is unique within a tenant. */
data class CoachingPolicy(
    var code: String,
    var name: String,
    var routeCode: String,
    var lane: String,
    var checksum: String,
    var fuelLiters: Double,
    var category: String,
    var vehicleId: String,
    var pinned: Boolean,
    var driverId: String,
    var status: CoachingPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingPolicyStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingPolicyStatus): Boolean {
        when (status) {
            CoachingPolicyStatus.IDLE -> return next == CoachingPolicyStatus.DRAINING || next == CoachingPolicyStatus.STOPPED
            CoachingPolicyStatus.WARMING -> return next == CoachingPolicyStatus.READY
            CoachingPolicyStatus.READY -> return next == CoachingPolicyStatus.STOPPED
            CoachingPolicyStatus.DRAINING -> {
                when (next) {
                    CoachingPolicyStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CoachingPolicyStatus) {
        if (!canMoveTo(next)) {
            error("coaching policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = fuelLiters

    fun snapshot(): CoachingPolicy {
        return CoachingPolicy(
            code = code,
            name = name,
            routeCode = routeCode,
            lane = lane,
            checksum = checksum,
            fuelLiters = fuelLiters,
            category = category,
            vehicleId = vehicleId,
            pinned = pinned,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a coaching policy in status idle. */
fun newCoachingPolicy(code: String, name: String): CoachingPolicy {
    return CoachingPolicy(
        code = code,
        name = name,
        routeCode = "",
        lane = "",
        checksum = "",
        fuelLiters = 0.0,
        category = "",
        vehicleId = "",
        pinned = false,
        driverId = "",
        status = CoachingPolicyStatus.IDLE,
    )
}
