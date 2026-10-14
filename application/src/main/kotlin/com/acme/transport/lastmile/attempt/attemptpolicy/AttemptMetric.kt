package com.acme.transport.lastmile.attempt.attemptpolicy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a attempt metric is in its life cycle. */
enum class AttemptMetricStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A attempt metric. Its code is unique within a tenant. */
data class AttemptMetric(
    var code: String,
    var label: String,
    var fuelLiters: Double,
    var ownerId: String,
    var capacity: Int,
    var description: String,
    var maximum: Double,
    var currency: String,
    var summary: String,
    var cost: Double,
    var status: AttemptMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AttemptMetricStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AttemptMetricStatus): Boolean {
        when (status) {
            AttemptMetricStatus.IDLE -> {
                when (next) {
                    AttemptMetricStatus.DRAINING -> return true
                }
                return false
            }
            AttemptMetricStatus.WARMING -> return next == AttemptMetricStatus.DRAINING || next == AttemptMetricStatus.STOPPED
            AttemptMetricStatus.READY -> return next == AttemptMetricStatus.DRAINING || next == AttemptMetricStatus.STOPPED
            AttemptMetricStatus.DRAINING -> {
                when (next) {
                    AttemptMetricStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AttemptMetricStatus) {
        if (!canMoveTo(next)) {
            error("attempt metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"attempt metric"} $code"

    fun effectiveLoad(): Double = round((maximum - capacity.toDouble()) * 100) / 100

    fun snapshot(): AttemptMetric {
        return AttemptMetric(
            code = code,
            label = label,
            fuelLiters = fuelLiters,
            ownerId = ownerId,
            capacity = capacity,
            description = description,
            maximum = maximum,
            currency = currency,
            summary = summary,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a attempt metric in status idle. */
fun newAttemptMetric(code: String, label: String): AttemptMetric {
    return AttemptMetric(
        code = code,
        label = label,
        fuelLiters = 0.0,
        ownerId = "",
        capacity = 0,
        description = "",
        maximum = 0.0,
        currency = "",
        summary = "",
        cost = 0.0,
        status = AttemptMetricStatus.IDLE,
    )
}
