package com.acme.transport.customs.clearance.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a clearance mapping is in its life cycle. */
enum class ClearanceMappingStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One clearance mapping, with what the planners need to act on it. */
data class ClearanceMapping(
    var code: String,
    var title: String,
    var capacity: Int,
    var limit: Int,
    var retries: Int,
    var approved: Boolean,
    var attributes: MutableMap<String, String>,
    var budget: Double,
    var rate: Double,
    var carrier: String,
    var status: ClearanceMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceMappingStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceMappingStatus): Boolean {
        when (status) {
            ClearanceMappingStatus.CREATED -> {
                when (next) {
                    ClearanceMappingStatus.PUBLISHED -> return true
                }
                return false
            }
            ClearanceMappingStatus.VALIDATED -> {
                when (next) {
                    ClearanceMappingStatus.RETIRED -> return true
                }
                return false
            }
            ClearanceMappingStatus.PUBLISHED -> {
                when (next) {
                    ClearanceMappingStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ClearanceMappingStatus) {
        if (!canMoveTo(next)) {
            error("clearance mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"clearance mapping"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedExposure(): Double = round((retries.toDouble() - limit.toDouble()) * 100) / 100

    fun snapshot(): ClearanceMapping {
        return ClearanceMapping(
            code = code,
            title = title,
            capacity = capacity,
            limit = limit,
            retries = retries,
            approved = approved,
            attributes = attributes,
            budget = budget,
            rate = rate,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a clearance mapping in status created. */
fun newClearanceMapping(code: String, title: String): ClearanceMapping {
    return ClearanceMapping(
        code = code,
        title = title,
        capacity = 0,
        limit = 0,
        retries = 0,
        approved = false,
        attributes = mutableMapOf<String, String>(),
        budget = 0.0,
        rate = 0.0,
        carrier = "",
        status = ClearanceMappingStatus.CREATED,
    )
}
