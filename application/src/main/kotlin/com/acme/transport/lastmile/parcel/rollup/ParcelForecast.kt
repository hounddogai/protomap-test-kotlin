package com.acme.transport.lastmile.parcel.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a parcel forecast is in its life cycle. */
enum class ParcelForecastStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A parcel forecast. Its code is unique within a tenant. */
data class ParcelForecast(
    var code: String,
    var name: String,
    var attempts: Int,
    var visible: Boolean,
    var active: Boolean,
    var floorValue: Double,
    var status: ParcelForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelForecastStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelForecastStatus): Boolean {
        when (status) {
            ParcelForecastStatus.IDLE -> {
                when (next) {
                    ParcelForecastStatus.WARMING, ParcelForecastStatus.READY -> return true
                }
                return false
            }
            ParcelForecastStatus.WARMING -> {
                when (next) {
                    ParcelForecastStatus.DRAINING -> return true
                }
                return false
            }
            ParcelForecastStatus.READY -> return next == ParcelForecastStatus.DRAINING || next == ParcelForecastStatus.STOPPED
            ParcelForecastStatus.DRAINING -> {
                when (next) {
                    ParcelForecastStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ParcelForecastStatus) {
        if (!canMoveTo(next)) {
            error("parcel forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun effectiveExposure(): Double = round((floorValue - attempts.toDouble()) * 100) / 100

    fun snapshot(): ParcelForecast {
        return ParcelForecast(
            code = code,
            name = name,
            attempts = attempts,
            visible = visible,
            active = active,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a parcel forecast in status idle. */
fun newParcelForecast(code: String, name: String): ParcelForecast {
    return ParcelForecast(
        code = code,
        name = name,
        attempts = 0,
        visible = false,
        active = false,
        floorValue = 0.0,
        status = ParcelForecastStatus.IDLE,
    )
}
