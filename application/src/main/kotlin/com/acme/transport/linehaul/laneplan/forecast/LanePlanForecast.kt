package com.acme.transport.linehaul.laneplan.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lane plan forecast records of linehaul's lane plan work, and the rules, stores, and calculations around
// them.

/** Where a lane plan forecast is in its life cycle. */
enum class LanePlanForecastStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A lane plan forecast. Its code is unique within a tenant. */
data class LanePlanForecast(
    var code: String,
    var title: String,
    var category: String,
    var shiftCode: String,
    var priority: Int,
    var maximum: Double,
    var source: String,
    var locale: String,
    var createdAt: Int,
    var status: LanePlanForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanForecastStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanForecastStatus): Boolean {
        when (status) {
            LanePlanForecastStatus.PLANNED -> return next == LanePlanForecastStatus.IN_PROGRESS
            LanePlanForecastStatus.SCHEDULED -> {
                when (next) {
                    LanePlanForecastStatus.IN_PROGRESS, LanePlanForecastStatus.CANCELLED -> return true
                }
                return false
            }
            LanePlanForecastStatus.IN_PROGRESS -> {
                when (next) {
                    LanePlanForecastStatus.COMPLETED -> return true
                }
                return false
            }
            LanePlanForecastStatus.COMPLETED -> {
                when (next) {
                    LanePlanForecastStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LanePlanForecastStatus) {
        if (!canMoveTo(next)) {
            error("lane plan forecast $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"lane plan forecast"} $code"

    fun weightedValue(): Double = round((createdAt.toDouble() - maximum) * 100) / 100

    fun snapshot(): LanePlanForecast {
        return LanePlanForecast(
            code = code,
            title = title,
            category = category,
            shiftCode = shiftCode,
            priority = priority,
            maximum = maximum,
            source = source,
            locale = locale,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a lane plan forecast in status planned. */
fun newLanePlanForecast(code: String, title: String): LanePlanForecast {
    return LanePlanForecast(
        code = code,
        title = title,
        category = "",
        shiftCode = "",
        priority = 0,
        maximum = 0.0,
        source = "",
        locale = "",
        createdAt = 0,
        status = LanePlanForecastStatus.PLANNED,
    )
}
