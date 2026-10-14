package com.acme.transport.linehaul.trailer.trailergroup

import com.acme.transport.kit.text.normalize

// The rolling trailer group records of linehaul's trailer work, and the rules, stores, and calculations
// around them.

/** Where a rolling trailer group is in its life cycle. */
enum class RollingTrailerGroupStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One rolling trailer group, with what the planners need to act on it. */
data class RollingTrailerGroup(
    var code: String,
    var label: String,
    var depot: String,
    var vehicleId: String,
    var attributes: MutableMap<String, String>,
    var fuelLiters: Double,
    var status: RollingTrailerGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingTrailerGroupStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingTrailerGroupStatus): Boolean {
        when (status) {
            RollingTrailerGroupStatus.CREATED -> {
                when (next) {
                    RollingTrailerGroupStatus.PUBLISHED, RollingTrailerGroupStatus.RETIRED -> return true
                }
                return false
            }
            RollingTrailerGroupStatus.VALIDATED -> {
                when (next) {
                    RollingTrailerGroupStatus.PUBLISHED, RollingTrailerGroupStatus.RETIRED -> return true
                }
                return false
            }
            RollingTrailerGroupStatus.PUBLISHED -> return next == RollingTrailerGroupStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RollingTrailerGroupStatus) {
        if (!canMoveTo(next)) {
            error("rolling trailer group $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = fuelLiters

    fun snapshot(): RollingTrailerGroup {
        return RollingTrailerGroup(
            code = code,
            label = label,
            depot = depot,
            vehicleId = vehicleId,
            attributes = attributes,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a rolling trailer group in status created. */
fun newRollingTrailerGroup(code: String, label: String): RollingTrailerGroup {
    return RollingTrailerGroup(
        code = code,
        label = label,
        depot = "",
        vehicleId = "",
        attributes = mutableMapOf<String, String>(),
        fuelLiters = 0.0,
        status = RollingTrailerGroupStatus.CREATED,
    )
}
