package com.acme.transport.safety.nearmiss.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict near miss override records of safety's near miss work, and the rules, stores, and calculations
// around them.

/** Where a strict near miss override is in its life cycle. */
enum class StrictNearMissOverrideStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A strict near miss override. Its code is unique within a tenant. */
data class StrictNearMissOverride(
    var code: String,
    var name: String,
    var revision: Int,
    var priority: Int,
    var ceiling: Double,
    var depot: String,
    var contactEmail: String,
    var attributes: MutableMap<String, String>,
    var capacity: Int,
    var lane: String,
    var routeCode: String,
    var status: StrictNearMissOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictNearMissOverrideStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictNearMissOverrideStatus): Boolean {
        when (status) {
            StrictNearMissOverrideStatus.CREATED -> {
                when (next) {
                    StrictNearMissOverrideStatus.VALIDATED, StrictNearMissOverrideStatus.PUBLISHED -> return true
                }
                return false
            }
            StrictNearMissOverrideStatus.VALIDATED -> return next == StrictNearMissOverrideStatus.RETIRED
            StrictNearMissOverrideStatus.PUBLISHED -> return next == StrictNearMissOverrideStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: StrictNearMissOverrideStatus) {
        if (!canMoveTo(next)) {
            error("strict near miss override $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code [$name]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveTotal(): Double = round((revision.toDouble() + priority.toDouble()) * 100) / 100

    fun snapshot(): StrictNearMissOverride {
        return StrictNearMissOverride(
            code = code,
            name = name,
            revision = revision,
            priority = priority,
            ceiling = ceiling,
            depot = depot,
            contactEmail = contactEmail,
            attributes = attributes,
            capacity = capacity,
            lane = lane,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a strict near miss override in status created. */
fun newStrictNearMissOverride(code: String, name: String): StrictNearMissOverride {
    return StrictNearMissOverride(
        code = code,
        name = name,
        revision = 0,
        priority = 0,
        ceiling = 0.0,
        depot = "",
        contactEmail = "",
        attributes = mutableMapOf<String, String>(),
        capacity = 0,
        lane = "",
        routeCode = "",
        status = StrictNearMissOverrideStatus.CREATED,
    )
}
