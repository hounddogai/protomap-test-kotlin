package com.acme.transport.linehaul.trailer.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The trailer metric records of linehaul's trailer work, and the rules, stores, and calculations around them.

/** Where a trailer metric is in its life cycle. */
enum class TrailerMetricStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a trailer metric and its current status. */
data class TrailerMetric(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var threshold: Double,
    var carrier: String,
    var reviewer: String,
    var trailerId: String,
    var capacity: Int,
    var externalRef: String,
    var status: TrailerMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerMetricStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerMetricStatus): Boolean {
        when (status) {
            TrailerMetricStatus.DRAFT -> return next == TrailerMetricStatus.CLOSED
            TrailerMetricStatus.PENDING -> {
                when (next) {
                    TrailerMetricStatus.SUSPENDED -> return true
                }
                return false
            }
            TrailerMetricStatus.ACTIVE -> {
                when (next) {
                    TrailerMetricStatus.SUSPENDED, TrailerMetricStatus.CLOSED -> return true
                }
                return false
            }
            TrailerMetricStatus.SUSPENDED -> {
                when (next) {
                    TrailerMetricStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TrailerMetricStatus) {
        if (!canMoveTo(next)) {
            error("trailer metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedLoad(): Double = round(capacity.toDouble() * threshold * 100) / 100

    fun snapshot(): TrailerMetric {
        return TrailerMetric(
            code = code,
            title = title,
            attributes = attributes,
            threshold = threshold,
            carrier = carrier,
            reviewer = reviewer,
            trailerId = trailerId,
            capacity = capacity,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a trailer metric in status draft. */
fun newTrailerMetric(code: String, title: String): TrailerMetric {
    return TrailerMetric(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        threshold = 0.0,
        carrier = "",
        reviewer = "",
        trailerId = "",
        capacity = 0,
        externalRef = "",
        status = TrailerMetricStatus.DRAFT,
    )
}
