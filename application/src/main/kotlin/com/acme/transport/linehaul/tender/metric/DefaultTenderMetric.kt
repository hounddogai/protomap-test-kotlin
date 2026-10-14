package com.acme.transport.linehaul.tender.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default tender metric records of linehaul's tender work, and the rules, stores, and calculations around
// them.

/** Where a default tender metric is in its life cycle. */
enum class DefaultTenderMetricStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a default tender metric and its current status. */
data class DefaultTenderMetric(
    var code: String,
    var label: String,
    var amount: Double,
    var archived: Boolean,
    var margin: Double,
    var attributes: MutableMap<String, String>,
    var fuelLiters: Double,
    var timezone: String,
    var status: DefaultTenderMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultTenderMetricStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultTenderMetricStatus): Boolean {
        when (status) {
            DefaultTenderMetricStatus.PLANNED -> return next == DefaultTenderMetricStatus.IN_PROGRESS
            DefaultTenderMetricStatus.SCHEDULED -> return next == DefaultTenderMetricStatus.IN_PROGRESS
            DefaultTenderMetricStatus.IN_PROGRESS -> return next == DefaultTenderMetricStatus.CANCELLED
            DefaultTenderMetricStatus.COMPLETED -> return next == DefaultTenderMetricStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: DefaultTenderMetricStatus) {
        if (!canMoveTo(next)) {
            error("default tender metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"default tender metric"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedValue(): Double = round((fuelLiters + margin) * 100) / 100

    fun snapshot(): DefaultTenderMetric {
        return DefaultTenderMetric(
            code = code,
            label = label,
            amount = amount,
            archived = archived,
            margin = margin,
            attributes = attributes,
            fuelLiters = fuelLiters,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a default tender metric in status planned. */
fun newDefaultTenderMetric(code: String, label: String): DefaultTenderMetric {
    return DefaultTenderMetric(
        code = code,
        label = label,
        amount = 0.0,
        archived = false,
        margin = 0.0,
        attributes = mutableMapOf<String, String>(),
        fuelLiters = 0.0,
        timezone = "",
        status = DefaultTenderMetricStatus.PLANNED,
    )
}
