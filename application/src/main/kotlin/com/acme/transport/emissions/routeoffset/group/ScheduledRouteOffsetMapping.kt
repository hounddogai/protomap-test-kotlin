package com.acme.transport.emissions.routeoffset.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled route offset mapping is in its life cycle. */
enum class ScheduledRouteOffsetMappingStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A scheduled route offset mapping as the route offset team keeps it. */
data class ScheduledRouteOffsetMapping(
    var code: String,
    var name: String,
    var category: String,
    var floorValue: Double,
    var version: Int,
    var quantity: Int,
    var status: ScheduledRouteOffsetMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledRouteOffsetMappingStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledRouteOffsetMappingStatus): Boolean {
        when (status) {
            ScheduledRouteOffsetMappingStatus.IDLE -> return next == ScheduledRouteOffsetMappingStatus.DRAINING || next == ScheduledRouteOffsetMappingStatus.STOPPED
            ScheduledRouteOffsetMappingStatus.WARMING -> {
                when (next) {
                    ScheduledRouteOffsetMappingStatus.READY, ScheduledRouteOffsetMappingStatus.STOPPED -> return true
                }
                return false
            }
            ScheduledRouteOffsetMappingStatus.READY -> {
                when (next) {
                    ScheduledRouteOffsetMappingStatus.DRAINING -> return true
                }
                return false
            }
            ScheduledRouteOffsetMappingStatus.DRAINING -> return next == ScheduledRouteOffsetMappingStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ScheduledRouteOffsetMappingStatus) {
        if (!canMoveTo(next)) {
            error("scheduled route offset mapping $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code: $name"

    fun weightedExposure(): Double = round((floorValue + quantity.toDouble()) * 100) / 100

    fun snapshot(): ScheduledRouteOffsetMapping {
        return ScheduledRouteOffsetMapping(
            code = code,
            name = name,
            category = category,
            floorValue = floorValue,
            version = version,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a scheduled route offset mapping in status idle. */
fun newScheduledRouteOffsetMapping(code: String, name: String): ScheduledRouteOffsetMapping {
    return ScheduledRouteOffsetMapping(
        code = code,
        name = name,
        category = "",
        floorValue = 0.0,
        version = 0,
        quantity = 0,
        status = ScheduledRouteOffsetMappingStatus.IDLE,
    )
}
