package com.acme.transport.safety.incident.defaultincidentmanifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict incident metric is in its life cycle. */
enum class StrictIncidentMetricStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A strict incident metric as the incident team keeps it. */
data class StrictIncidentMetric(
    var code: String,
    var label: String,
    var graceDays: Int,
    var shiftCode: String,
    var etaMinutes: Int,
    var status: StrictIncidentMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictIncidentMetricStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictIncidentMetricStatus): Boolean {
        when (status) {
            StrictIncidentMetricStatus.DRAFT -> {
                when (next) {
                    StrictIncidentMetricStatus.ACTIVE, StrictIncidentMetricStatus.SUSPENDED -> return true
                }
                return false
            }
            StrictIncidentMetricStatus.PENDING -> return next == StrictIncidentMetricStatus.ACTIVE || next == StrictIncidentMetricStatus.SUSPENDED
            StrictIncidentMetricStatus.ACTIVE -> return next == StrictIncidentMetricStatus.CLOSED
            StrictIncidentMetricStatus.SUSPENDED -> {
                when (next) {
                    StrictIncidentMetricStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictIncidentMetricStatus) {
        if (!canMoveTo(next)) {
            error("strict incident metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveVolume(): Double = round(graceDays.toDouble() * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): StrictIncidentMetric {
        return StrictIncidentMetric(
            code = code,
            label = label,
            graceDays = graceDays,
            shiftCode = shiftCode,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a strict incident metric in status draft. */
fun newStrictIncidentMetric(code: String, label: String): StrictIncidentMetric {
    return StrictIncidentMetric(
        code = code,
        label = label,
        graceDays = 0,
        shiftCode = "",
        etaMinutes = 0,
        status = StrictIncidentMetricStatus.DRAFT,
    )
}
