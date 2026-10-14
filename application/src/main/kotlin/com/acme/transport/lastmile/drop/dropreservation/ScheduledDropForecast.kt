package com.acme.transport.lastmile.drop.dropreservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled drop forecast is in its life cycle. */
enum class ScheduledDropForecastStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A scheduled drop forecast as the drop team keeps it. */
data class ScheduledDropForecast(
    var code: String,
    var name: String,
    var checksum: String,
    var active: Boolean,
    var etaMinutes: Int,
    var updatedAt: Int,
    var quantity: Int,
    var depot: String,
    var weight: Double,
    var lane: String,
    var status: ScheduledDropForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledDropForecastStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledDropForecastStatus): Boolean {
        when (status) {
            ScheduledDropForecastStatus.NEW -> return next == ScheduledDropForecastStatus.QUEUED || next == ScheduledDropForecastStatus.DONE
            ScheduledDropForecastStatus.QUEUED -> return next == ScheduledDropForecastStatus.DONE || next == ScheduledDropForecastStatus.FAILED
            ScheduledDropForecastStatus.RUNNING -> return next == ScheduledDropForecastStatus.DONE
            ScheduledDropForecastStatus.DONE -> {
                when (next) {
                    ScheduledDropForecastStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledDropForecastStatus) {
        if (!canMoveTo(next)) {
            error("scheduled drop forecast $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun projectedVolume(): Double = round(weight * quantity.toDouble() * 100) / 100

    fun snapshot(): ScheduledDropForecast {
        return ScheduledDropForecast(
            code = code,
            name = name,
            checksum = checksum,
            active = active,
            etaMinutes = etaMinutes,
            updatedAt = updatedAt,
            quantity = quantity,
            depot = depot,
            weight = weight,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a scheduled drop forecast in status new. */
fun newScheduledDropForecast(code: String, name: String): ScheduledDropForecast {
    return ScheduledDropForecast(
        code = code,
        name = name,
        checksum = "",
        active = false,
        etaMinutes = 0,
        updatedAt = 0,
        quantity = 0,
        depot = "",
        weight = 0.0,
        lane = "",
        status = ScheduledDropForecastStatus.NEW,
    )
}
