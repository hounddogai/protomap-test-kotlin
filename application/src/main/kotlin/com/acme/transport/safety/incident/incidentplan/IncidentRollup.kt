package com.acme.transport.safety.incident.incidentplan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a incident rollup is in its life cycle. */
enum class IncidentRollupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One incident rollup, with what the planners need to act on it. */
data class IncidentRollup(
    var code: String,
    var label: String,
    var attempts: Int,
    var checksum: String,
    var capacity: Int,
    var country: String,
    var minimum: Double,
    var weight: Double,
    var status: IncidentRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentRollupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentRollupStatus): Boolean {
        when (status) {
            IncidentRollupStatus.PLANNED -> return next == IncidentRollupStatus.CANCELLED
            IncidentRollupStatus.SCHEDULED -> return next == IncidentRollupStatus.IN_PROGRESS || next == IncidentRollupStatus.CANCELLED
            IncidentRollupStatus.IN_PROGRESS -> {
                when (next) {
                    IncidentRollupStatus.CANCELLED -> return true
                }
                return false
            }
            IncidentRollupStatus.COMPLETED -> {
                when (next) {
                    IncidentRollupStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IncidentRollupStatus) {
        if (!canMoveTo(next)) {
            error("incident rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"incident rollup"} $code"

    fun weightedValue(): Double = round((attempts.toDouble() + weight) * 100) / 100

    fun snapshot(): IncidentRollup {
        return IncidentRollup(
            code = code,
            label = label,
            attempts = attempts,
            checksum = checksum,
            capacity = capacity,
            country = country,
            minimum = minimum,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a incident rollup in status planned. */
fun newIncidentRollup(code: String, label: String): IncidentRollup {
    return IncidentRollup(
        code = code,
        label = label,
        attempts = 0,
        checksum = "",
        capacity = 0,
        country = "",
        minimum = 0.0,
        weight = 0.0,
        status = IncidentRollupStatus.PLANNED,
    )
}
