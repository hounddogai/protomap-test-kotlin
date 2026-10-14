package com.acme.transport.emissions.idling.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled idling variance is in its life cycle. */
enum class ScheduledIdlingVarianceStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One scheduled idling variance, with what the planners need to act on it. */
data class ScheduledIdlingVariance(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var unit: String,
    var maximum: Double,
    var depot: String,
    var attempts: Int,
    var contactEmail: String,
    var status: ScheduledIdlingVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledIdlingVarianceStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledIdlingVarianceStatus): Boolean {
        when (status) {
            ScheduledIdlingVarianceStatus.REQUESTED -> return next == ScheduledIdlingVarianceStatus.REVOKED || next == ScheduledIdlingVarianceStatus.EXPIRED
            ScheduledIdlingVarianceStatus.GRANTED -> return next == ScheduledIdlingVarianceStatus.REVOKED
            ScheduledIdlingVarianceStatus.REVOKED -> {
                when (next) {
                    ScheduledIdlingVarianceStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledIdlingVarianceStatus) {
        if (!canMoveTo(next)) {
            error("scheduled idling variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"scheduled idling variance"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedVolume(): Double = round((attempts.toDouble() + maximum) * 100) / 100

    fun snapshot(): ScheduledIdlingVariance {
        return ScheduledIdlingVariance(
            code = code,
            title = title,
            attributes = attributes,
            unit = unit,
            maximum = maximum,
            depot = depot,
            attempts = attempts,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a scheduled idling variance in status requested. */
fun newScheduledIdlingVariance(code: String, title: String): ScheduledIdlingVariance {
    return ScheduledIdlingVariance(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        unit = "",
        maximum = 0.0,
        depot = "",
        attempts = 0,
        contactEmail = "",
        status = ScheduledIdlingVarianceStatus.REQUESTED,
    )
}
