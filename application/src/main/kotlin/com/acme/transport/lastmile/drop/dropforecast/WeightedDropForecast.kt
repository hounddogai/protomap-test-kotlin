package com.acme.transport.lastmile.drop.dropforecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted drop forecast records of lastmile's drop work, and the rules, stores, and calculations around
// them.

/** Where a weighted drop forecast is in its life cycle. */
enum class WeightedDropForecastStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A weighted drop forecast. Its code is unique within a tenant. */
data class WeightedDropForecast(
    var code: String,
    var title: String,
    var version: Int,
    var label: String,
    var updatedAt: Int,
    var depot: String,
    var carrier: String,
    var stopCount: Int,
    var visible: Boolean,
    var archived: Boolean,
    var dock: String,
    var status: WeightedDropForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedDropForecastStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedDropForecastStatus): Boolean {
        when (status) {
            WeightedDropForecastStatus.OPEN -> return next == WeightedDropForecastStatus.REOPENED || next == WeightedDropForecastStatus.ARCHIVED
            WeightedDropForecastStatus.IN_REVIEW -> return next == WeightedDropForecastStatus.REOPENED
            WeightedDropForecastStatus.RESOLVED -> return next == WeightedDropForecastStatus.REOPENED || next == WeightedDropForecastStatus.ARCHIVED
            WeightedDropForecastStatus.REOPENED -> return next == WeightedDropForecastStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: WeightedDropForecastStatus) {
        if (!canMoveTo(next)) {
            error("weighted drop forecast $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "$code ($title)"

    fun adjustedValue(): Double = round(updatedAt.toDouble() * stopCount.toDouble() * 100) / 100

    fun snapshot(): WeightedDropForecast {
        return WeightedDropForecast(
            code = code,
            title = title,
            version = version,
            label = label,
            updatedAt = updatedAt,
            depot = depot,
            carrier = carrier,
            stopCount = stopCount,
            visible = visible,
            archived = archived,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a weighted drop forecast in status open. */
fun newWeightedDropForecast(code: String, title: String): WeightedDropForecast {
    return WeightedDropForecast(
        code = code,
        title = title,
        version = 0,
        label = "",
        updatedAt = 0,
        depot = "",
        carrier = "",
        stopCount = 0,
        visible = false,
        archived = false,
        dock = "",
        status = WeightedDropForecastStatus.OPEN,
    )
}
