package com.acme.transport.customs.inspection.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a inspection forecast is in its life cycle. */
enum class InspectionForecastStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One inspection forecast, with what the planners need to act on it. */
data class InspectionForecast(
    var code: String,
    var title: String,
    var unit: String,
    var summary: String,
    var attributes: MutableMap<String, String>,
    var cost: Double,
    var pallets: Int,
    var sequence: Int,
    var shiftCode: String,
    var name: String,
    var weight: Double,
    var status: InspectionForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionForecastStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionForecastStatus): Boolean {
        when (status) {
            InspectionForecastStatus.REQUESTED -> {
                when (next) {
                    InspectionForecastStatus.EXPIRED -> return true
                }
                return false
            }
            InspectionForecastStatus.GRANTED -> return next == InspectionForecastStatus.REVOKED || next == InspectionForecastStatus.EXPIRED
            InspectionForecastStatus.REVOKED -> return next == InspectionForecastStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: InspectionForecastStatus) {
        if (!canMoveTo(next)) {
            error("inspection forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"inspection forecast"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveLoad(): Double = round((weight + pallets.toDouble()) * 100) / 100

    fun snapshot(): InspectionForecast {
        return InspectionForecast(
            code = code,
            title = title,
            unit = unit,
            summary = summary,
            attributes = attributes,
            cost = cost,
            pallets = pallets,
            sequence = sequence,
            shiftCode = shiftCode,
            name = name,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a inspection forecast in status requested. */
fun newInspectionForecast(code: String, title: String): InspectionForecast {
    return InspectionForecast(
        code = code,
        title = title,
        unit = "",
        summary = "",
        attributes = mutableMapOf<String, String>(),
        cost = 0.0,
        pallets = 0,
        sequence = 0,
        shiftCode = "",
        name = "",
        weight = 0.0,
        status = InspectionForecastStatus.REQUESTED,
    )
}
