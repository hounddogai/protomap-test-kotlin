package com.acme.transport.freightpay.accessorial.accessorialentry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a accessorial forecast is in its life cycle. */
enum class AccessorialForecastStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a accessorial forecast and its current status. */
data class AccessorialForecast(
    var code: String,
    var title: String,
    var expiresAt: Int,
    var name: String,
    var ceiling: Double,
    var checksum: String,
    var fuelLiters: Double,
    var windowDays: Int,
    var sequence: Int,
    var routeCode: String,
    var dock: String,
    var status: AccessorialForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialForecastStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialForecastStatus): Boolean {
        when (status) {
            AccessorialForecastStatus.DRAFT -> {
                when (next) {
                    AccessorialForecastStatus.CLOSED -> return true
                }
                return false
            }
            AccessorialForecastStatus.PENDING -> return next == AccessorialForecastStatus.ACTIVE
            AccessorialForecastStatus.ACTIVE -> return next == AccessorialForecastStatus.SUSPENDED || next == AccessorialForecastStatus.CLOSED
            AccessorialForecastStatus.SUSPENDED -> return next == AccessorialForecastStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: AccessorialForecastStatus) {
        if (!canMoveTo(next)) {
            error("accessorial forecast $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun adjustedLoad(): Double = round((expiresAt.toDouble() - windowDays.toDouble()) * 100) / 100

    fun snapshot(): AccessorialForecast {
        return AccessorialForecast(
            code = code,
            title = title,
            expiresAt = expiresAt,
            name = name,
            ceiling = ceiling,
            checksum = checksum,
            fuelLiters = fuelLiters,
            windowDays = windowDays,
            sequence = sequence,
            routeCode = routeCode,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a accessorial forecast in status draft. */
fun newAccessorialForecast(code: String, title: String): AccessorialForecast {
    return AccessorialForecast(
        code = code,
        title = title,
        expiresAt = 0,
        name = "",
        ceiling = 0.0,
        checksum = "",
        fuelLiters = 0.0,
        windowDays = 0,
        sequence = 0,
        routeCode = "",
        dock = "",
        status = AccessorialForecastStatus.DRAFT,
    )
}
