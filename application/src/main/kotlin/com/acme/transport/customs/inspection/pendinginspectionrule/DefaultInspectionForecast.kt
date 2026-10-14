package com.acme.transport.customs.inspection.pendinginspectionrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default inspection forecast is in its life cycle. */
enum class DefaultInspectionForecastStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a default inspection forecast and its current status. */
data class DefaultInspectionForecast(
    var code: String,
    var name: String,
    var capacity: Int,
    var batchSize: Int,
    var graceDays: Int,
    var fuelLiters: Double,
    var active: Boolean,
    var distanceKm: Double,
    var contactEmail: String,
    var status: DefaultInspectionForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultInspectionForecastStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultInspectionForecastStatus): Boolean {
        when (status) {
            DefaultInspectionForecastStatus.CREATED -> {
                when (next) {
                    DefaultInspectionForecastStatus.VALIDATED, DefaultInspectionForecastStatus.RETIRED -> return true
                }
                return false
            }
            DefaultInspectionForecastStatus.VALIDATED -> return next == DefaultInspectionForecastStatus.PUBLISHED || next == DefaultInspectionForecastStatus.RETIRED
            DefaultInspectionForecastStatus.PUBLISHED -> {
                when (next) {
                    DefaultInspectionForecastStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultInspectionForecastStatus) {
        if (!canMoveTo(next)) {
            error("default inspection forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun netExposure(): Double = round(distanceKm * graceDays.toDouble() * 100) / 100

    fun snapshot(): DefaultInspectionForecast {
        return DefaultInspectionForecast(
            code = code,
            name = name,
            capacity = capacity,
            batchSize = batchSize,
            graceDays = graceDays,
            fuelLiters = fuelLiters,
            active = active,
            distanceKm = distanceKm,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a default inspection forecast in status created. */
fun newDefaultInspectionForecast(code: String, name: String): DefaultInspectionForecast {
    return DefaultInspectionForecast(
        code = code,
        name = name,
        capacity = 0,
        batchSize = 0,
        graceDays = 0,
        fuelLiters = 0.0,
        active = false,
        distanceKm = 0.0,
        contactEmail = "",
        status = DefaultInspectionForecastStatus.CREATED,
    )
}
