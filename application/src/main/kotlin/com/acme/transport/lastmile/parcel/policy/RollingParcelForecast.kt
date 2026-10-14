package com.acme.transport.lastmile.parcel.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling parcel forecast is in its life cycle. */
enum class RollingParcelForecastStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A rolling parcel forecast as the parcel team keeps it. */
data class RollingParcelForecast(
    var code: String,
    var title: String,
    var ratio: Double,
    var externalRef: String,
    var graceDays: Int,
    var fuelLiters: Double,
    var status: RollingParcelForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingParcelForecastStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingParcelForecastStatus): Boolean {
        when (status) {
            RollingParcelForecastStatus.CREATED -> {
                when (next) {
                    RollingParcelForecastStatus.VALIDATED -> return true
                }
                return false
            }
            RollingParcelForecastStatus.VALIDATED -> return next == RollingParcelForecastStatus.RETIRED
            RollingParcelForecastStatus.PUBLISHED -> {
                when (next) {
                    RollingParcelForecastStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingParcelForecastStatus) {
        if (!canMoveTo(next)) {
            error("rolling parcel forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun effectiveValue(): Double = round((fuelLiters + ratio) * 100) / 100

    fun snapshot(): RollingParcelForecast {
        return RollingParcelForecast(
            code = code,
            title = title,
            ratio = ratio,
            externalRef = externalRef,
            graceDays = graceDays,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling parcel forecast in status created. */
fun newRollingParcelForecast(code: String, title: String): RollingParcelForecast {
    return RollingParcelForecast(
        code = code,
        title = title,
        ratio = 0.0,
        externalRef = "",
        graceDays = 0,
        fuelLiters = 0.0,
        status = RollingParcelForecastStatus.CREATED,
    )
}
