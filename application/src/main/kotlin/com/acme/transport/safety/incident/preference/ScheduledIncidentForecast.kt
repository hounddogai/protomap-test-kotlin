package com.acme.transport.safety.incident.preference

import com.acme.transport.kit.text.normalize

/** Where a scheduled incident forecast is in its life cycle. */
enum class ScheduledIncidentForecastStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a scheduled incident forecast and its current status. */
data class ScheduledIncidentForecast(
    var code: String,
    var name: String,
    var retries: Int,
    var externalRef: String,
    var title: String,
    var carrier: String,
    var summary: String,
    var timezone: String,
    var status: ScheduledIncidentForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledIncidentForecastStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledIncidentForecastStatus): Boolean {
        when (status) {
            ScheduledIncidentForecastStatus.CREATED -> return next == ScheduledIncidentForecastStatus.VALIDATED
            ScheduledIncidentForecastStatus.VALIDATED -> {
                when (next) {
                    ScheduledIncidentForecastStatus.PUBLISHED, ScheduledIncidentForecastStatus.RETIRED -> return true
                }
                return false
            }
            ScheduledIncidentForecastStatus.PUBLISHED -> return next == ScheduledIncidentForecastStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ScheduledIncidentForecastStatus) {
        if (!canMoveTo(next)) {
            error("scheduled incident forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"scheduled incident forecast"} $code"

    fun measuredValue(): Double = retries.toDouble()

    fun snapshot(): ScheduledIncidentForecast {
        return ScheduledIncidentForecast(
            code = code,
            name = name,
            retries = retries,
            externalRef = externalRef,
            title = title,
            carrier = carrier,
            summary = summary,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled incident forecast in status created. */
fun newScheduledIncidentForecast(code: String, name: String): ScheduledIncidentForecast {
    return ScheduledIncidentForecast(
        code = code,
        name = name,
        retries = 0,
        externalRef = "",
        title = "",
        carrier = "",
        summary = "",
        timezone = "",
        status = ScheduledIncidentForecastStatus.CREATED,
    )
}
