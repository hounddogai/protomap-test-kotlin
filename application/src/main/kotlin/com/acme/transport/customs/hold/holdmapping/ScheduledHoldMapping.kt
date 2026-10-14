package com.acme.transport.customs.hold.holdmapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled hold mapping records of customs's hold work, and the rules, stores, and calculations around
// them.

/** Where a scheduled hold mapping is in its life cycle. */
enum class ScheduledHoldMappingStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a scheduled hold mapping and its current status. */
data class ScheduledHoldMapping(
    var code: String,
    var label: String,
    var threshold: Double,
    var reviewer: String,
    var approved: Boolean,
    var startsAt: Int,
    var carrier: String,
    var distanceKm: Double,
    var maximum: Double,
    var routeCode: String,
    var status: ScheduledHoldMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledHoldMappingStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledHoldMappingStatus): Boolean {
        when (status) {
            ScheduledHoldMappingStatus.OPEN -> return next == ScheduledHoldMappingStatus.IN_REVIEW || next == ScheduledHoldMappingStatus.REOPENED
            ScheduledHoldMappingStatus.IN_REVIEW -> {
                when (next) {
                    ScheduledHoldMappingStatus.ARCHIVED -> return true
                }
                return false
            }
            ScheduledHoldMappingStatus.RESOLVED -> {
                when (next) {
                    ScheduledHoldMappingStatus.REOPENED, ScheduledHoldMappingStatus.ARCHIVED -> return true
                }
                return false
            }
            ScheduledHoldMappingStatus.REOPENED -> return next == ScheduledHoldMappingStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ScheduledHoldMappingStatus) {
        if (!canMoveTo(next)) {
            error("scheduled hold mapping $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"scheduled hold mapping"} $code"

    fun effectiveTotal(): Double = round((threshold - distanceKm) * 100) / 100

    fun snapshot(): ScheduledHoldMapping {
        return ScheduledHoldMapping(
            code = code,
            label = label,
            threshold = threshold,
            reviewer = reviewer,
            approved = approved,
            startsAt = startsAt,
            carrier = carrier,
            distanceKm = distanceKm,
            maximum = maximum,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a scheduled hold mapping in status open. */
fun newScheduledHoldMapping(code: String, label: String): ScheduledHoldMapping {
    return ScheduledHoldMapping(
        code = code,
        label = label,
        threshold = 0.0,
        reviewer = "",
        approved = false,
        startsAt = 0,
        carrier = "",
        distanceKm = 0.0,
        maximum = 0.0,
        routeCode = "",
        status = ScheduledHoldMappingStatus.OPEN,
    )
}
