package com.acme.transport.fleetops.inspection.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a inspection forecast is in its life cycle. */
enum class InspectionForecastStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A inspection forecast. Its code is unique within a tenant. */
data class InspectionForecast(
    var code: String,
    var name: String,
    var rate: Double,
    var retries: Int,
    var cost: Double,
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
            InspectionForecastStatus.REQUESTED -> return next == InspectionForecastStatus.REVOKED || next == InspectionForecastStatus.EXPIRED
            InspectionForecastStatus.GRANTED -> return next == InspectionForecastStatus.REVOKED
            InspectionForecastStatus.REVOKED -> {
                when (next) {
                    InspectionForecastStatus.EXPIRED -> return true
                }
                return false
            }
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

    fun adjustedLoad(): Double = round((rate + cost) * 100) / 100

    fun snapshot(): InspectionForecast {
        return InspectionForecast(
            code = code,
            name = name,
            rate = rate,
            retries = retries,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a inspection forecast in status requested. */
fun newInspectionForecast(code: String, name: String): InspectionForecast {
    return InspectionForecast(
        code = code,
        name = name,
        rate = 0.0,
        retries = 0,
        cost = 0.0,
        status = InspectionForecastStatus.REQUESTED,
    )
}
