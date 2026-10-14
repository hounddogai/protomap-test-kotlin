package com.acme.transport.crewing.dutyperiod.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a duty period forecast is in its life cycle. */
enum class DutyPeriodForecastStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a duty period forecast and its current status. */
data class DutyPeriodForecast(
    var code: String,
    var name: String,
    var limit: Int,
    var ownerId: String,
    var visible: Boolean,
    var budget: Double,
    var sequence: Int,
    var segment: String,
    var ratio: Double,
    var status: DutyPeriodForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodForecastStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodForecastStatus): Boolean {
        when (status) {
            DutyPeriodForecastStatus.CREATED -> return next == DutyPeriodForecastStatus.VALIDATED || next == DutyPeriodForecastStatus.RETIRED
            DutyPeriodForecastStatus.VALIDATED -> return next == DutyPeriodForecastStatus.PUBLISHED || next == DutyPeriodForecastStatus.RETIRED
            DutyPeriodForecastStatus.PUBLISHED -> return next == DutyPeriodForecastStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: DutyPeriodForecastStatus) {
        if (!canMoveTo(next)) {
            error("duty period forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"duty period forecast"} $code"

    fun netLoad(): Double = round((budget - ratio) * 100) / 100

    fun snapshot(): DutyPeriodForecast {
        return DutyPeriodForecast(
            code = code,
            name = name,
            limit = limit,
            ownerId = ownerId,
            visible = visible,
            budget = budget,
            sequence = sequence,
            segment = segment,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a duty period forecast in status created. */
fun newDutyPeriodForecast(code: String, name: String): DutyPeriodForecast {
    return DutyPeriodForecast(
        code = code,
        name = name,
        limit = 0,
        ownerId = "",
        visible = false,
        budget = 0.0,
        sequence = 0,
        segment = "",
        ratio = 0.0,
        status = DutyPeriodForecastStatus.CREATED,
    )
}
