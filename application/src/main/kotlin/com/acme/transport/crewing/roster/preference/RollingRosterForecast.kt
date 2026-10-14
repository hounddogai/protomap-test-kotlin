package com.acme.transport.crewing.roster.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling roster forecast is in its life cycle. */
enum class RollingRosterForecastStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A rolling roster forecast. Its code is unique within a tenant. */
data class RollingRosterForecast(
    var code: String,
    var title: String,
    var depot: String,
    var quantity: Int,
    var currency: String,
    var limit: Int,
    var etaMinutes: Int,
    var createdAt: Int,
    var pallets: Int,
    var floorValue: Double,
    var locale: String,
    var status: RollingRosterForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingRosterForecastStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingRosterForecastStatus): Boolean {
        when (status) {
            RollingRosterForecastStatus.OPEN -> return next == RollingRosterForecastStatus.ARCHIVED
            RollingRosterForecastStatus.IN_REVIEW -> {
                when (next) {
                    RollingRosterForecastStatus.REOPENED, RollingRosterForecastStatus.ARCHIVED -> return true
                }
                return false
            }
            RollingRosterForecastStatus.RESOLVED -> {
                when (next) {
                    RollingRosterForecastStatus.REOPENED -> return true
                }
                return false
            }
            RollingRosterForecastStatus.REOPENED -> return next == RollingRosterForecastStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RollingRosterForecastStatus) {
        if (!canMoveTo(next)) {
            error("rolling roster forecast $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun adjustedExposure(): Double = round(floorValue * quantity.toDouble() * 100) / 100

    fun snapshot(): RollingRosterForecast {
        return RollingRosterForecast(
            code = code,
            title = title,
            depot = depot,
            quantity = quantity,
            currency = currency,
            limit = limit,
            etaMinutes = etaMinutes,
            createdAt = createdAt,
            pallets = pallets,
            floorValue = floorValue,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a rolling roster forecast in status open. */
fun newRollingRosterForecast(code: String, title: String): RollingRosterForecast {
    return RollingRosterForecast(
        code = code,
        title = title,
        depot = "",
        quantity = 0,
        currency = "",
        limit = 0,
        etaMinutes = 0,
        createdAt = 0,
        pallets = 0,
        floorValue = 0.0,
        locale = "",
        status = RollingRosterForecastStatus.OPEN,
    )
}
