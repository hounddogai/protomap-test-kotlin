package com.acme.transport.safety.incident.preference

import com.acme.transport.kit.text.normalize

/** Where a legacy incident metric is in its life cycle. */
enum class LegacyIncidentMetricStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A legacy incident metric as the incident team keeps it. */
data class LegacyIncidentMetric(
    var code: String,
    var title: String,
    var active: Boolean,
    var pinned: Boolean,
    var minimum: Double,
    var label: String,
    var trailerId: String,
    var status: LegacyIncidentMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyIncidentMetricStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyIncidentMetricStatus): Boolean {
        when (status) {
            LegacyIncidentMetricStatus.CREATED -> {
                when (next) {
                    LegacyIncidentMetricStatus.VALIDATED, LegacyIncidentMetricStatus.PUBLISHED -> return true
                }
                return false
            }
            LegacyIncidentMetricStatus.VALIDATED -> return next == LegacyIncidentMetricStatus.RETIRED
            LegacyIncidentMetricStatus.PUBLISHED -> return next == LegacyIncidentMetricStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: LegacyIncidentMetricStatus) {
        if (!canMoveTo(next)) {
            error("legacy incident metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"legacy incident metric"} $code"

    fun measuredValue(): Double = minimum

    fun snapshot(): LegacyIncidentMetric {
        return LegacyIncidentMetric(
            code = code,
            title = title,
            active = active,
            pinned = pinned,
            minimum = minimum,
            label = label,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a legacy incident metric in status created. */
fun newLegacyIncidentMetric(code: String, title: String): LegacyIncidentMetric {
    return LegacyIncidentMetric(
        code = code,
        title = title,
        active = false,
        pinned = false,
        minimum = 0.0,
        label = "",
        trailerId = "",
        status = LegacyIncidentMetricStatus.CREATED,
    )
}
