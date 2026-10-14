package com.acme.transport.safety.incident.incidentclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a incident estimate is in its life cycle. */
enum class IncidentEstimateStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A incident estimate as the incident team keeps it. */
data class IncidentEstimate(
    var code: String,
    var title: String,
    var channel: String,
    var minimum: Double,
    var attributes: MutableMap<String, String>,
    var rate: Double,
    var odometer: Int,
    var ceiling: Double,
    var distanceKm: Double,
    var status: IncidentEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentEstimateStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentEstimateStatus): Boolean {
        when (status) {
            IncidentEstimateStatus.PROPOSED -> {
                when (next) {
                    IncidentEstimateStatus.WITHDRAWN -> return true
                }
                return false
            }
            IncidentEstimateStatus.APPROVED -> return next == IncidentEstimateStatus.REJECTED
            IncidentEstimateStatus.REJECTED -> return next == IncidentEstimateStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: IncidentEstimateStatus) {
        if (!canMoveTo(next)) {
            error("incident estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netVolume(): Double = round((odometer.toDouble() - rate) * 100) / 100

    fun snapshot(): IncidentEstimate {
        return IncidentEstimate(
            code = code,
            title = title,
            channel = channel,
            minimum = minimum,
            attributes = attributes,
            rate = rate,
            odometer = odometer,
            ceiling = ceiling,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a incident estimate in status proposed. */
fun newIncidentEstimate(code: String, title: String): IncidentEstimate {
    return IncidentEstimate(
        code = code,
        title = title,
        channel = "",
        minimum = 0.0,
        attributes = mutableMapOf<String, String>(),
        rate = 0.0,
        odometer = 0,
        ceiling = 0.0,
        distanceKm = 0.0,
        status = IncidentEstimateStatus.PROPOSED,
    )
}
