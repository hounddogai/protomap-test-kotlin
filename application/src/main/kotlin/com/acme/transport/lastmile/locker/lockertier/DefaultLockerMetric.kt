package com.acme.transport.lastmile.locker.lockertier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default locker metric is in its life cycle. */
enum class DefaultLockerMetricStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A default locker metric. Its code is unique within a tenant. */
data class DefaultLockerMetric(
    var code: String,
    var label: String,
    var rate: Double,
    var attributes: MutableMap<String, String>,
    var locale: String,
    var threshold: Double,
    var source: String,
    var status: DefaultLockerMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultLockerMetricStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultLockerMetricStatus): Boolean {
        when (status) {
            DefaultLockerMetricStatus.PLANNED -> return next == DefaultLockerMetricStatus.COMPLETED
            DefaultLockerMetricStatus.SCHEDULED -> {
                when (next) {
                    DefaultLockerMetricStatus.IN_PROGRESS -> return true
                }
                return false
            }
            DefaultLockerMetricStatus.IN_PROGRESS -> {
                when (next) {
                    DefaultLockerMetricStatus.CANCELLED -> return true
                }
                return false
            }
            DefaultLockerMetricStatus.COMPLETED -> {
                when (next) {
                    DefaultLockerMetricStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultLockerMetricStatus) {
        if (!canMoveTo(next)) {
            error("default locker metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedVolume(): Double = round((threshold - rate) * 100) / 100

    fun snapshot(): DefaultLockerMetric {
        return DefaultLockerMetric(
            code = code,
            label = label,
            rate = rate,
            attributes = attributes,
            locale = locale,
            threshold = threshold,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a default locker metric in status planned. */
fun newDefaultLockerMetric(code: String, label: String): DefaultLockerMetric {
    return DefaultLockerMetric(
        code = code,
        label = label,
        rate = 0.0,
        attributes = mutableMapOf<String, String>(),
        locale = "",
        threshold = 0.0,
        source = "",
        status = DefaultLockerMetricStatus.PLANNED,
    )
}
