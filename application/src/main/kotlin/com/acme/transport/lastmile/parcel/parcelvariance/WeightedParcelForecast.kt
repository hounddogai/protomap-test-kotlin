package com.acme.transport.lastmile.parcel.parcelvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted parcel forecast is in its life cycle. */
enum class WeightedParcelForecastStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One weighted parcel forecast, with what the planners need to act on it. */
data class WeightedParcelForecast(
    var code: String,
    var title: String,
    var odometer: Int,
    var expiresAt: Int,
    var shiftCode: String,
    var checksum: String,
    var lane: String,
    var depot: String,
    var description: String,
    var timezone: String,
    var status: WeightedParcelForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedParcelForecastStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedParcelForecastStatus): Boolean {
        when (status) {
            WeightedParcelForecastStatus.PROPOSED -> {
                when (next) {
                    WeightedParcelForecastStatus.REJECTED -> return true
                }
                return false
            }
            WeightedParcelForecastStatus.APPROVED -> {
                when (next) {
                    WeightedParcelForecastStatus.REJECTED, WeightedParcelForecastStatus.WITHDRAWN -> return true
                }
                return false
            }
            WeightedParcelForecastStatus.REJECTED -> {
                when (next) {
                    WeightedParcelForecastStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedParcelForecastStatus) {
        if (!canMoveTo(next)) {
            error("weighted parcel forecast $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun effectiveTotal(): Double = round((expiresAt.toDouble() - odometer.toDouble()) * 100) / 100

    fun snapshot(): WeightedParcelForecast {
        return WeightedParcelForecast(
            code = code,
            title = title,
            odometer = odometer,
            expiresAt = expiresAt,
            shiftCode = shiftCode,
            checksum = checksum,
            lane = lane,
            depot = depot,
            description = description,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a weighted parcel forecast in status proposed. */
fun newWeightedParcelForecast(code: String, title: String): WeightedParcelForecast {
    return WeightedParcelForecast(
        code = code,
        title = title,
        odometer = 0,
        expiresAt = 0,
        shiftCode = "",
        checksum = "",
        lane = "",
        depot = "",
        description = "",
        timezone = "",
        status = WeightedParcelForecastStatus.PROPOSED,
    )
}
