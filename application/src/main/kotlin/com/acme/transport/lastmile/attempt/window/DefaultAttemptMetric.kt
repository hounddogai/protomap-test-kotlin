package com.acme.transport.lastmile.attempt.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default attempt metric is in its life cycle. */
enum class DefaultAttemptMetricStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A default attempt metric as the attempt team keeps it. */
data class DefaultAttemptMetric(
    var code: String,
    var name: String,
    var ceiling: Double,
    var threshold: Double,
    var attempts: Int,
    var durationMinutes: Int,
    var weight: Double,
    var rate: Double,
    var maximum: Double,
    var status: DefaultAttemptMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultAttemptMetricStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultAttemptMetricStatus): Boolean {
        when (status) {
            DefaultAttemptMetricStatus.DRAFT -> {
                when (next) {
                    DefaultAttemptMetricStatus.SUSPENDED, DefaultAttemptMetricStatus.CLOSED -> return true
                }
                return false
            }
            DefaultAttemptMetricStatus.PENDING -> {
                when (next) {
                    DefaultAttemptMetricStatus.SUSPENDED, DefaultAttemptMetricStatus.CLOSED -> return true
                }
                return false
            }
            DefaultAttemptMetricStatus.ACTIVE -> return next == DefaultAttemptMetricStatus.SUSPENDED
            DefaultAttemptMetricStatus.SUSPENDED -> return next == DefaultAttemptMetricStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: DefaultAttemptMetricStatus) {
        if (!canMoveTo(next)) {
            error("default attempt metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun adjustedExposure(): Double = round(weight * attempts.toDouble() * 100) / 100

    fun snapshot(): DefaultAttemptMetric {
        return DefaultAttemptMetric(
            code = code,
            name = name,
            ceiling = ceiling,
            threshold = threshold,
            attempts = attempts,
            durationMinutes = durationMinutes,
            weight = weight,
            rate = rate,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a default attempt metric in status draft. */
fun newDefaultAttemptMetric(code: String, name: String): DefaultAttemptMetric {
    return DefaultAttemptMetric(
        code = code,
        name = name,
        ceiling = 0.0,
        threshold = 0.0,
        attempts = 0,
        durationMinutes = 0,
        weight = 0.0,
        rate = 0.0,
        maximum = 0.0,
        status = DefaultAttemptMetricStatus.DRAFT,
    )
}
