package com.acme.transport.emissions.routeoffset.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a route offset policy is in its life cycle. */
enum class RouteOffsetPolicyStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A route offset policy as the route offset team keeps it. */
data class RouteOffsetPolicy(
    var code: String,
    var name: String,
    var maximum: Double,
    var batchSize: Int,
    var revision: Int,
    var windowDays: Int,
    var stopCount: Int,
    var source: String,
    var retries: Int,
    var active: Boolean,
    var priority: Int,
    var status: RouteOffsetPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetPolicyStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetPolicyStatus): Boolean {
        when (status) {
            RouteOffsetPolicyStatus.IDLE -> return next == RouteOffsetPolicyStatus.STOPPED
            RouteOffsetPolicyStatus.WARMING -> return next == RouteOffsetPolicyStatus.DRAINING || next == RouteOffsetPolicyStatus.STOPPED
            RouteOffsetPolicyStatus.READY -> {
                when (next) {
                    RouteOffsetPolicyStatus.DRAINING, RouteOffsetPolicyStatus.STOPPED -> return true
                }
                return false
            }
            RouteOffsetPolicyStatus.DRAINING -> {
                when (next) {
                    RouteOffsetPolicyStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RouteOffsetPolicyStatus) {
        if (!canMoveTo(next)) {
            error("route offset policy $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code [$name]"

    fun adjustedTotal(): Double = round((retries.toDouble() + revision.toDouble()) * 100) / 100

    fun snapshot(): RouteOffsetPolicy {
        return RouteOffsetPolicy(
            code = code,
            name = name,
            maximum = maximum,
            batchSize = batchSize,
            revision = revision,
            windowDays = windowDays,
            stopCount = stopCount,
            source = source,
            retries = retries,
            active = active,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a route offset policy in status idle. */
fun newRouteOffsetPolicy(code: String, name: String): RouteOffsetPolicy {
    return RouteOffsetPolicy(
        code = code,
        name = name,
        maximum = 0.0,
        batchSize = 0,
        revision = 0,
        windowDays = 0,
        stopCount = 0,
        source = "",
        retries = 0,
        active = false,
        priority = 0,
        status = RouteOffsetPolicyStatus.IDLE,
    )
}
