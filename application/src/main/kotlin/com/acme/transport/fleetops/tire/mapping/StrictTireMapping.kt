package com.acme.transport.fleetops.tire.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict tire mapping records of fleetops's tire work, and the rules, stores, and calculations around
// them.

/** Where a strict tire mapping is in its life cycle. */
enum class StrictTireMappingStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A strict tire mapping as the tire team keeps it. */
data class StrictTireMapping(
    var code: String,
    var name: String,
    var depot: String,
    var margin: Double,
    var priority: Int,
    var ceiling: Double,
    var carrier: String,
    var tags: MutableList<String>,
    var etaMinutes: Int,
    var minimum: Double,
    var status: StrictTireMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictTireMappingStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictTireMappingStatus): Boolean {
        when (status) {
            StrictTireMappingStatus.CREATED -> return next == StrictTireMappingStatus.PUBLISHED
            StrictTireMappingStatus.VALIDATED -> return next == StrictTireMappingStatus.PUBLISHED || next == StrictTireMappingStatus.RETIRED
            StrictTireMappingStatus.PUBLISHED -> return next == StrictTireMappingStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: StrictTireMappingStatus) {
        if (!canMoveTo(next)) {
            error("strict tire mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"strict tire mapping"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedValue(): Double = round((margin - priority.toDouble()) * 100) / 100

    fun snapshot(): StrictTireMapping {
        return StrictTireMapping(
            code = code,
            name = name,
            depot = depot,
            margin = margin,
            priority = priority,
            ceiling = ceiling,
            carrier = carrier,
            tags = tags,
            etaMinutes = etaMinutes,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a strict tire mapping in status created. */
fun newStrictTireMapping(code: String, name: String): StrictTireMapping {
    return StrictTireMapping(
        code = code,
        name = name,
        depot = "",
        margin = 0.0,
        priority = 0,
        ceiling = 0.0,
        carrier = "",
        tags = mutableListOf<String>(),
        etaMinutes = 0,
        minimum = 0.0,
        status = StrictTireMappingStatus.CREATED,
    )
}
