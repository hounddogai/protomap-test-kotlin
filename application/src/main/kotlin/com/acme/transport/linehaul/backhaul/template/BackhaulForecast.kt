package com.acme.transport.linehaul.backhaul.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a backhaul forecast is in its life cycle. */
enum class BackhaulForecastStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A backhaul forecast. Its code is unique within a tenant. */
data class BackhaulForecast(
    var code: String,
    var title: String,
    var reviewer: String,
    var segment: String,
    var rate: Double,
    var label: String,
    var weight: Double,
    var stopCount: Int,
    var lane: String,
    var status: BackhaulForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BackhaulForecastStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BackhaulForecastStatus): Boolean {
        when (status) {
            BackhaulForecastStatus.PROPOSED -> return next == BackhaulForecastStatus.WITHDRAWN
            BackhaulForecastStatus.APPROVED -> return next == BackhaulForecastStatus.REJECTED || next == BackhaulForecastStatus.WITHDRAWN
            BackhaulForecastStatus.REJECTED -> return next == BackhaulForecastStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: BackhaulForecastStatus) {
        if (!canMoveTo(next)) {
            error("backhaul forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $title"

    fun netVolume(): Double = round((rate + stopCount.toDouble()) * 100) / 100

    fun snapshot(): BackhaulForecast {
        return BackhaulForecast(
            code = code,
            title = title,
            reviewer = reviewer,
            segment = segment,
            rate = rate,
            label = label,
            weight = weight,
            stopCount = stopCount,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a backhaul forecast in status proposed. */
fun newBackhaulForecast(code: String, title: String): BackhaulForecast {
    return BackhaulForecast(
        code = code,
        title = title,
        reviewer = "",
        segment = "",
        rate = 0.0,
        label = "",
        weight = 0.0,
        stopCount = 0,
        lane = "",
        status = BackhaulForecastStatus.PROPOSED,
    )
}
