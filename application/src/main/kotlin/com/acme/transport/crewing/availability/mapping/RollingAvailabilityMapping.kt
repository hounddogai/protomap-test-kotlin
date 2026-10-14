package com.acme.transport.crewing.availability.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling availability mapping records of crewing's availability work, and the rules, stores, and
// calculations around them.

/** Where a rolling availability mapping is in its life cycle. */
enum class RollingAvailabilityMappingStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A rolling availability mapping. Its code is unique within a tenant. */
data class RollingAvailabilityMapping(
    var code: String,
    var name: String,
    var attributes: MutableMap<String, String>,
    var retries: Int,
    var cost: Double,
    var driverId: String,
    var routeCode: String,
    var reviewer: String,
    var priority: Int,
    var status: RollingAvailabilityMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingAvailabilityMappingStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingAvailabilityMappingStatus): Boolean {
        when (status) {
            RollingAvailabilityMappingStatus.PROPOSED -> return next == RollingAvailabilityMappingStatus.APPROVED
            RollingAvailabilityMappingStatus.APPROVED -> return next == RollingAvailabilityMappingStatus.REJECTED || next == RollingAvailabilityMappingStatus.WITHDRAWN
            RollingAvailabilityMappingStatus.REJECTED -> return next == RollingAvailabilityMappingStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: RollingAvailabilityMappingStatus) {
        if (!canMoveTo(next)) {
            error("rolling availability mapping $code cannot move from $status to $next")
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

    fun effectiveExposure(): Double = round((cost + retries.toDouble()) * 100) / 100

    fun snapshot(): RollingAvailabilityMapping {
        return RollingAvailabilityMapping(
            code = code,
            name = name,
            attributes = attributes,
            retries = retries,
            cost = cost,
            driverId = driverId,
            routeCode = routeCode,
            reviewer = reviewer,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rolling availability mapping in status proposed. */
fun newRollingAvailabilityMapping(code: String, name: String): RollingAvailabilityMapping {
    return RollingAvailabilityMapping(
        code = code,
        name = name,
        attributes = mutableMapOf<String, String>(),
        retries = 0,
        cost = 0.0,
        driverId = "",
        routeCode = "",
        reviewer = "",
        priority = 0,
        status = RollingAvailabilityMappingStatus.PROPOSED,
    )
}
