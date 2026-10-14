package com.acme.transport.safety.incident.metric

import com.acme.transport.kit.text.normalize

// The incident metric records of safety's incident work, and the rules, stores, and calculations around them.

/** Where a incident metric is in its life cycle. */
enum class IncidentMetricStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A incident metric as the incident team keeps it. */
data class IncidentMetric(
    var code: String,
    var label: String,
    var notes: String,
    var sequence: Int,
    var driverId: String,
    var vehicleId: String,
    var status: IncidentMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentMetricStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentMetricStatus): Boolean {
        when (status) {
            IncidentMetricStatus.PLANNED -> {
                when (next) {
                    IncidentMetricStatus.SCHEDULED, IncidentMetricStatus.IN_PROGRESS -> return true
                }
                return false
            }
            IncidentMetricStatus.SCHEDULED -> {
                when (next) {
                    IncidentMetricStatus.IN_PROGRESS, IncidentMetricStatus.COMPLETED -> return true
                }
                return false
            }
            IncidentMetricStatus.IN_PROGRESS -> {
                when (next) {
                    IncidentMetricStatus.CANCELLED -> return true
                }
                return false
            }
            IncidentMetricStatus.COMPLETED -> {
                when (next) {
                    IncidentMetricStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IncidentMetricStatus) {
        if (!canMoveTo(next)) {
            error("incident metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = sequence.toDouble()

    fun snapshot(): IncidentMetric {
        return IncidentMetric(
            code = code,
            label = label,
            notes = notes,
            sequence = sequence,
            driverId = driverId,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a incident metric in status planned. */
fun newIncidentMetric(code: String, label: String): IncidentMetric {
    return IncidentMetric(
        code = code,
        label = label,
        notes = "",
        sequence = 0,
        driverId = "",
        vehicleId = "",
        status = IncidentMetricStatus.PLANNED,
    )
}
