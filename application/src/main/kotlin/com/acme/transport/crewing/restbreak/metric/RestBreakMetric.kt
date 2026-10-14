package com.acme.transport.crewing.restbreak.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rest break metric records of crewing's rest break work, and the rules, stores, and calculations around
// them.

/** Where a rest break metric is in its life cycle. */
enum class RestBreakMetricStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One rest break metric, with what the planners need to act on it. */
data class RestBreakMetric(
    var code: String,
    var name: String,
    var timezone: String,
    var trailerId: String,
    var fuelLiters: Double,
    var region: String,
    var createdAt: Int,
    var externalRef: String,
    var rate: Double,
    var status: RestBreakMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakMetricStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakMetricStatus): Boolean {
        when (status) {
            RestBreakMetricStatus.PROPOSED -> {
                when (next) {
                    RestBreakMetricStatus.REJECTED, RestBreakMetricStatus.WITHDRAWN -> return true
                }
                return false
            }
            RestBreakMetricStatus.APPROVED -> {
                when (next) {
                    RestBreakMetricStatus.REJECTED -> return true
                }
                return false
            }
            RestBreakMetricStatus.REJECTED -> {
                when (next) {
                    RestBreakMetricStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RestBreakMetricStatus) {
        if (!canMoveTo(next)) {
            error("rest break metric $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun projectedTotal(): Double = round((rate - createdAt.toDouble()) * 100) / 100

    fun snapshot(): RestBreakMetric {
        return RestBreakMetric(
            code = code,
            name = name,
            timezone = timezone,
            trailerId = trailerId,
            fuelLiters = fuelLiters,
            region = region,
            createdAt = createdAt,
            externalRef = externalRef,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a rest break metric in status proposed. */
fun newRestBreakMetric(code: String, name: String): RestBreakMetric {
    return RestBreakMetric(
        code = code,
        name = name,
        timezone = "",
        trailerId = "",
        fuelLiters = 0.0,
        region = "",
        createdAt = 0,
        externalRef = "",
        rate = 0.0,
        status = RestBreakMetricStatus.PROPOSED,
    )
}
