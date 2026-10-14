package com.acme.transport.safety.incident.incidentmanifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a incident variance is in its life cycle. */
enum class IncidentVarianceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One incident variance, with what the planners need to act on it. */
data class IncidentVariance(
    var code: String,
    var name: String,
    var expiresAt: Int,
    var routeCode: String,
    var phone: String,
    var fuelLiters: Double,
    var maximum: Double,
    var segment: String,
    var ratio: Double,
    var timezone: String,
    var status: IncidentVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentVarianceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentVarianceStatus): Boolean {
        when (status) {
            IncidentVarianceStatus.PLANNED -> {
                when (next) {
                    IncidentVarianceStatus.IN_PROGRESS -> return true
                }
                return false
            }
            IncidentVarianceStatus.SCHEDULED -> return next == IncidentVarianceStatus.IN_PROGRESS
            IncidentVarianceStatus.IN_PROGRESS -> {
                when (next) {
                    IncidentVarianceStatus.CANCELLED -> return true
                }
                return false
            }
            IncidentVarianceStatus.COMPLETED -> {
                when (next) {
                    IncidentVarianceStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IncidentVarianceStatus) {
        if (!canMoveTo(next)) {
            error("incident variance $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun adjustedVolume(): Double = round((maximum - fuelLiters) * 100) / 100

    fun snapshot(): IncidentVariance {
        return IncidentVariance(
            code = code,
            name = name,
            expiresAt = expiresAt,
            routeCode = routeCode,
            phone = phone,
            fuelLiters = fuelLiters,
            maximum = maximum,
            segment = segment,
            ratio = ratio,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a incident variance in status planned. */
fun newIncidentVariance(code: String, name: String): IncidentVariance {
    return IncidentVariance(
        code = code,
        name = name,
        expiresAt = 0,
        routeCode = "",
        phone = "",
        fuelLiters = 0.0,
        maximum = 0.0,
        segment = "",
        ratio = 0.0,
        timezone = "",
        status = IncidentVarianceStatus.PLANNED,
    )
}
