package com.acme.transport.network.transittime.metric

import com.acme.transport.kit.text.normalize

// The primary transit time metric records of network's transit time work, and the rules, stores, and
// calculations around them.

/** Where a primary transit time metric is in its life cycle. */
enum class PrimaryTransitTimeMetricStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A primary transit time metric as the transit time team keeps it. */
data class PrimaryTransitTimeMetric(
    var code: String,
    var name: String,
    var trailerId: String,
    var stopCount: Int,
    var country: String,
    var status: PrimaryTransitTimeMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryTransitTimeMetricStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryTransitTimeMetricStatus): Boolean {
        when (status) {
            PrimaryTransitTimeMetricStatus.DRAFT -> return next == PrimaryTransitTimeMetricStatus.CLOSED
            PrimaryTransitTimeMetricStatus.PENDING -> {
                when (next) {
                    PrimaryTransitTimeMetricStatus.ACTIVE, PrimaryTransitTimeMetricStatus.SUSPENDED -> return true
                }
                return false
            }
            PrimaryTransitTimeMetricStatus.ACTIVE -> return next == PrimaryTransitTimeMetricStatus.SUSPENDED
            PrimaryTransitTimeMetricStatus.SUSPENDED -> return next == PrimaryTransitTimeMetricStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: PrimaryTransitTimeMetricStatus) {
        if (!canMoveTo(next)) {
            error("primary transit time metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun measuredValue(): Double = stopCount.toDouble()

    fun snapshot(): PrimaryTransitTimeMetric {
        return PrimaryTransitTimeMetric(
            code = code,
            name = name,
            trailerId = trailerId,
            stopCount = stopCount,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a primary transit time metric in status draft. */
fun newPrimaryTransitTimeMetric(code: String, name: String): PrimaryTransitTimeMetric {
    return PrimaryTransitTimeMetric(
        code = code,
        name = name,
        trailerId = "",
        stopCount = 0,
        country = "",
        status = PrimaryTransitTimeMetricStatus.DRAFT,
    )
}
