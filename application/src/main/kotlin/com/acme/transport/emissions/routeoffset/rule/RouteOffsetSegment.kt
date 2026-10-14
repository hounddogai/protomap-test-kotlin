package com.acme.transport.emissions.routeoffset.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a route offset segment is in its life cycle. */
enum class RouteOffsetSegmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A route offset segment as the route offset team keeps it. */
data class RouteOffsetSegment(
    var code: String,
    var name: String,
    var stopCount: Int,
    var attributes: MutableMap<String, String>,
    var label: String,
    var sequence: Int,
    var maximum: Double,
    var cost: Double,
    var status: RouteOffsetSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetSegmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetSegmentStatus): Boolean {
        when (status) {
            RouteOffsetSegmentStatus.REQUESTED -> return next == RouteOffsetSegmentStatus.GRANTED || next == RouteOffsetSegmentStatus.EXPIRED
            RouteOffsetSegmentStatus.GRANTED -> return next == RouteOffsetSegmentStatus.REVOKED || next == RouteOffsetSegmentStatus.EXPIRED
            RouteOffsetSegmentStatus.REVOKED -> return next == RouteOffsetSegmentStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RouteOffsetSegmentStatus) {
        if (!canMoveTo(next)) {
            error("route offset segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"route offset segment"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveVolume(): Double = round((stopCount.toDouble() + sequence.toDouble()) * 100) / 100

    fun snapshot(): RouteOffsetSegment {
        return RouteOffsetSegment(
            code = code,
            name = name,
            stopCount = stopCount,
            attributes = attributes,
            label = label,
            sequence = sequence,
            maximum = maximum,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a route offset segment in status requested. */
fun newRouteOffsetSegment(code: String, name: String): RouteOffsetSegment {
    return RouteOffsetSegment(
        code = code,
        name = name,
        stopCount = 0,
        attributes = mutableMapOf<String, String>(),
        label = "",
        sequence = 0,
        maximum = 0.0,
        cost = 0.0,
        status = RouteOffsetSegmentStatus.REQUESTED,
    )
}
