package com.acme.transport.terminal.dwell.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive dwell metric records of terminal's dwell work, and the rules, stores, and calculations around
// them.

/** Where a adaptive dwell metric is in its life cycle. */
enum class AdaptiveDwellMetricStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One adaptive dwell metric, with what the planners need to act on it. */
data class AdaptiveDwellMetric(
    var code: String,
    var name: String,
    var endsAt: Int,
    var attributes: MutableMap<String, String>,
    var depot: String,
    var windowDays: Int,
    var source: String,
    var country: String,
    var rate: Double,
    var label: String,
    var status: AdaptiveDwellMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveDwellMetricStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveDwellMetricStatus): Boolean {
        when (status) {
            AdaptiveDwellMetricStatus.OPEN -> {
                when (next) {
                    AdaptiveDwellMetricStatus.ARCHIVED -> return true
                }
                return false
            }
            AdaptiveDwellMetricStatus.IN_REVIEW -> return next == AdaptiveDwellMetricStatus.REOPENED
            AdaptiveDwellMetricStatus.RESOLVED -> {
                when (next) {
                    AdaptiveDwellMetricStatus.REOPENED -> return true
                }
                return false
            }
            AdaptiveDwellMetricStatus.REOPENED -> {
                when (next) {
                    AdaptiveDwellMetricStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveDwellMetricStatus) {
        if (!canMoveTo(next)) {
            error("adaptive dwell metric $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$name]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netValue(): Double = round((windowDays.toDouble() + rate) * 100) / 100

    fun snapshot(): AdaptiveDwellMetric {
        return AdaptiveDwellMetric(
            code = code,
            name = name,
            endsAt = endsAt,
            attributes = attributes,
            depot = depot,
            windowDays = windowDays,
            source = source,
            country = country,
            rate = rate,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a adaptive dwell metric in status open. */
fun newAdaptiveDwellMetric(code: String, name: String): AdaptiveDwellMetric {
    return AdaptiveDwellMetric(
        code = code,
        name = name,
        endsAt = 0,
        attributes = mutableMapOf<String, String>(),
        depot = "",
        windowDays = 0,
        source = "",
        country = "",
        rate = 0.0,
        label = "",
        status = AdaptiveDwellMetricStatus.OPEN,
    )
}
