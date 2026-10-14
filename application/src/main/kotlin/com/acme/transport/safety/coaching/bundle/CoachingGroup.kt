package com.acme.transport.safety.coaching.bundle

import com.acme.transport.kit.text.normalize

/** Where a coaching group is in its life cycle. */
enum class CoachingGroupStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a coaching group and its current status. */
data class CoachingGroup(
    var code: String,
    var name: String,
    var attributes: MutableMap<String, String>,
    var discount: Double,
    var vehicleId: String,
    var checksum: String,
    var driverId: String,
    var status: CoachingGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingGroupStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingGroupStatus): Boolean {
        when (status) {
            CoachingGroupStatus.PROPOSED -> {
                when (next) {
                    CoachingGroupStatus.APPROVED -> return true
                }
                return false
            }
            CoachingGroupStatus.APPROVED -> return next == CoachingGroupStatus.WITHDRAWN
            CoachingGroupStatus.REJECTED -> return next == CoachingGroupStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: CoachingGroupStatus) {
        if (!canMoveTo(next)) {
            error("coaching group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = discount

    fun snapshot(): CoachingGroup {
        return CoachingGroup(
            code = code,
            name = name,
            attributes = attributes,
            discount = discount,
            vehicleId = vehicleId,
            checksum = checksum,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a coaching group in status proposed. */
fun newCoachingGroup(code: String, name: String): CoachingGroup {
    return CoachingGroup(
        code = code,
        name = name,
        attributes = mutableMapOf<String, String>(),
        discount = 0.0,
        vehicleId = "",
        checksum = "",
        driverId = "",
        status = CoachingGroupStatus.PROPOSED,
    )
}
