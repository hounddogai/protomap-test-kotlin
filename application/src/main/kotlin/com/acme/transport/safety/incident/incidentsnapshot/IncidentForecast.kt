package com.acme.transport.safety.incident.incidentsnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a incident forecast is in its life cycle. */
enum class IncidentForecastStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A incident forecast. Its code is unique within a tenant. */
data class IncidentForecast(
    var code: String,
    var title: String,
    var discount: Double,
    var windowDays: Int,
    var depot: String,
    var archived: Boolean,
    var graceDays: Int,
    var startsAt: Int,
    var ownerId: String,
    var loadKg: Double,
    var status: IncidentForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentForecastStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentForecastStatus): Boolean {
        when (status) {
            IncidentForecastStatus.PROPOSED -> {
                when (next) {
                    IncidentForecastStatus.REJECTED, IncidentForecastStatus.WITHDRAWN -> return true
                }
                return false
            }
            IncidentForecastStatus.APPROVED -> {
                when (next) {
                    IncidentForecastStatus.REJECTED -> return true
                }
                return false
            }
            IncidentForecastStatus.REJECTED -> return next == IncidentForecastStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: IncidentForecastStatus) {
        if (!canMoveTo(next)) {
            error("incident forecast $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun netVolume(): Double = round((loadKg - discount) * 100) / 100

    fun snapshot(): IncidentForecast {
        return IncidentForecast(
            code = code,
            title = title,
            discount = discount,
            windowDays = windowDays,
            depot = depot,
            archived = archived,
            graceDays = graceDays,
            startsAt = startsAt,
            ownerId = ownerId,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a incident forecast in status proposed. */
fun newIncidentForecast(code: String, title: String): IncidentForecast {
    return IncidentForecast(
        code = code,
        title = title,
        discount = 0.0,
        windowDays = 0,
        depot = "",
        archived = false,
        graceDays = 0,
        startsAt = 0,
        ownerId = "",
        loadKg = 0.0,
        status = IncidentForecastStatus.PROPOSED,
    )
}
