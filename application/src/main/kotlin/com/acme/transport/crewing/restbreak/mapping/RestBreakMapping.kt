package com.acme.transport.crewing.restbreak.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rest break mapping records of crewing's rest break work, and the rules, stores, and calculations around
// them.

/** Where a rest break mapping is in its life cycle. */
enum class RestBreakMappingStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a rest break mapping and its current status. */
data class RestBreakMapping(
    var code: String,
    var name: String,
    var depot: String,
    var timezone: String,
    var startsAt: Int,
    var label: String,
    var sequence: Int,
    var stopCount: Int,
    var priority: Int,
    var driverId: String,
    var minimum: Double,
    var status: RestBreakMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakMappingStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakMappingStatus): Boolean {
        when (status) {
            RestBreakMappingStatus.PROPOSED -> {
                when (next) {
                    RestBreakMappingStatus.APPROVED, RestBreakMappingStatus.WITHDRAWN -> return true
                }
                return false
            }
            RestBreakMappingStatus.APPROVED -> {
                when (next) {
                    RestBreakMappingStatus.REJECTED, RestBreakMappingStatus.WITHDRAWN -> return true
                }
                return false
            }
            RestBreakMappingStatus.REJECTED -> {
                when (next) {
                    RestBreakMappingStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RestBreakMappingStatus) {
        if (!canMoveTo(next)) {
            error("rest break mapping $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $name"

    fun projectedExposure(): Double = round((sequence.toDouble() + startsAt.toDouble()) * 100) / 100

    fun snapshot(): RestBreakMapping {
        return RestBreakMapping(
            code = code,
            name = name,
            depot = depot,
            timezone = timezone,
            startsAt = startsAt,
            label = label,
            sequence = sequence,
            stopCount = stopCount,
            priority = priority,
            driverId = driverId,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a rest break mapping in status proposed. */
fun newRestBreakMapping(code: String, name: String): RestBreakMapping {
    return RestBreakMapping(
        code = code,
        name = name,
        depot = "",
        timezone = "",
        startsAt = 0,
        label = "",
        sequence = 0,
        stopCount = 0,
        priority = 0,
        driverId = "",
        minimum = 0.0,
        status = RestBreakMappingStatus.PROPOSED,
    )
}
