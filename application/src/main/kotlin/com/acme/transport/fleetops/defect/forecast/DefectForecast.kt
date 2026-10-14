package com.acme.transport.fleetops.defect.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The defect forecast records of fleetops's defect work, and the rules, stores, and calculations around them.

/** Where a defect forecast is in its life cycle. */
enum class DefectForecastStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a defect forecast and its current status. */
data class DefectForecast(
    var code: String,
    var title: String,
    var name: String,
    var ratio: Double,
    var currency: String,
    var createdAt: Int,
    var loadKg: Double,
    var dock: String,
    var fuelLiters: Double,
    var active: Boolean,
    var status: DefectForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectForecastStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectForecastStatus): Boolean {
        when (status) {
            DefectForecastStatus.CREATED -> return next == DefectForecastStatus.PUBLISHED
            DefectForecastStatus.VALIDATED -> return next == DefectForecastStatus.PUBLISHED
            DefectForecastStatus.PUBLISHED -> return next == DefectForecastStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: DefectForecastStatus) {
        if (!canMoveTo(next)) {
            error("defect forecast $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"defect forecast"} $code"

    fun weightedLoad(): Double = round((createdAt.toDouble() + ratio) * 100) / 100

    fun snapshot(): DefectForecast {
        return DefectForecast(
            code = code,
            title = title,
            name = name,
            ratio = ratio,
            currency = currency,
            createdAt = createdAt,
            loadKg = loadKg,
            dock = dock,
            fuelLiters = fuelLiters,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a defect forecast in status created. */
fun newDefectForecast(code: String, title: String): DefectForecast {
    return DefectForecast(
        code = code,
        title = title,
        name = "",
        ratio = 0.0,
        currency = "",
        createdAt = 0,
        loadKg = 0.0,
        dock = "",
        fuelLiters = 0.0,
        active = false,
        status = DefectForecastStatus.CREATED,
    )
}
