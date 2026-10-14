package com.acme.transport.network.servicearea.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service area forecast is in its life cycle. */
enum class ServiceAreaForecastStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A service area forecast as the service area team keeps it. */
data class ServiceAreaForecast(
    var code: String,
    var title: String,
    var etaMinutes: Int,
    var tags: MutableList<String>,
    var startsAt: Int,
    var name: String,
    var driverId: String,
    var unit: String,
    var notes: String,
    var status: ServiceAreaForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaForecastStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaForecastStatus): Boolean {
        when (status) {
            ServiceAreaForecastStatus.DRAFT -> return next == ServiceAreaForecastStatus.SUSPENDED
            ServiceAreaForecastStatus.PENDING -> return next == ServiceAreaForecastStatus.ACTIVE || next == ServiceAreaForecastStatus.CLOSED
            ServiceAreaForecastStatus.ACTIVE -> return next == ServiceAreaForecastStatus.SUSPENDED
            ServiceAreaForecastStatus.SUSPENDED -> return next == ServiceAreaForecastStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ServiceAreaForecastStatus) {
        if (!canMoveTo(next)) {
            error("service area forecast $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedExposure(): Double = round((etaMinutes.toDouble() - startsAt.toDouble()) * 100) / 100

    fun snapshot(): ServiceAreaForecast {
        return ServiceAreaForecast(
            code = code,
            title = title,
            etaMinutes = etaMinutes,
            tags = tags,
            startsAt = startsAt,
            name = name,
            driverId = driverId,
            unit = unit,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a service area forecast in status draft. */
fun newServiceAreaForecast(code: String, title: String): ServiceAreaForecast {
    return ServiceAreaForecast(
        code = code,
        title = title,
        etaMinutes = 0,
        tags = mutableListOf<String>(),
        startsAt = 0,
        name = "",
        driverId = "",
        unit = "",
        notes = "",
        status = ServiceAreaForecastStatus.DRAFT,
    )
}
