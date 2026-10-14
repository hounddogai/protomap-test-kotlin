package com.acme.transport.lastmile.drop.dropentry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback drop metric is in its life cycle. */
enum class FallbackDropMetricStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A fallback drop metric. Its code is unique within a tenant. */
data class FallbackDropMetric(
    var code: String,
    var label: String,
    var margin: Double,
    var country: String,
    var reviewer: String,
    var trailerId: String,
    var attempts: Int,
    var externalRef: String,
    var channel: String,
    var floorValue: Double,
    var batchSize: Int,
    var status: FallbackDropMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackDropMetricStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackDropMetricStatus): Boolean {
        when (status) {
            FallbackDropMetricStatus.PROPOSED -> {
                when (next) {
                    FallbackDropMetricStatus.REJECTED, FallbackDropMetricStatus.WITHDRAWN -> return true
                }
                return false
            }
            FallbackDropMetricStatus.APPROVED -> {
                when (next) {
                    FallbackDropMetricStatus.REJECTED, FallbackDropMetricStatus.WITHDRAWN -> return true
                }
                return false
            }
            FallbackDropMetricStatus.REJECTED -> return next == FallbackDropMetricStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: FallbackDropMetricStatus) {
        if (!canMoveTo(next)) {
            error("fallback drop metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedVolume(): Double = round(floorValue * attempts.toDouble() * 100) / 100

    fun snapshot(): FallbackDropMetric {
        return FallbackDropMetric(
            code = code,
            label = label,
            margin = margin,
            country = country,
            reviewer = reviewer,
            trailerId = trailerId,
            attempts = attempts,
            externalRef = externalRef,
            channel = channel,
            floorValue = floorValue,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a fallback drop metric in status proposed. */
fun newFallbackDropMetric(code: String, label: String): FallbackDropMetric {
    return FallbackDropMetric(
        code = code,
        label = label,
        margin = 0.0,
        country = "",
        reviewer = "",
        trailerId = "",
        attempts = 0,
        externalRef = "",
        channel = "",
        floorValue = 0.0,
        batchSize = 0,
        status = FallbackDropMetricStatus.PROPOSED,
    )
}
