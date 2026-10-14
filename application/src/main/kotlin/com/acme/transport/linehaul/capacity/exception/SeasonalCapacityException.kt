package com.acme.transport.linehaul.capacity.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal capacity exception records of linehaul's capacity work, and the rules, stores, and
// calculations around them.

/** Where a seasonal capacity exception is in its life cycle. */
enum class SeasonalCapacityExceptionStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a seasonal capacity exception and its current status. */
data class SeasonalCapacityException(
    var code: String,
    var name: String,
    var distanceKm: Double,
    var source: String,
    var weight: Double,
    var timezone: String,
    var rate: Double,
    var currency: String,
    var reviewer: String,
    var limit: Int,
    var routeCode: String,
    var status: SeasonalCapacityExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalCapacityExceptionStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalCapacityExceptionStatus): Boolean {
        when (status) {
            SeasonalCapacityExceptionStatus.DRAFT -> {
                when (next) {
                    SeasonalCapacityExceptionStatus.ACTIVE, SeasonalCapacityExceptionStatus.SUSPENDED -> return true
                }
                return false
            }
            SeasonalCapacityExceptionStatus.PENDING -> {
                when (next) {
                    SeasonalCapacityExceptionStatus.ACTIVE, SeasonalCapacityExceptionStatus.SUSPENDED -> return true
                }
                return false
            }
            SeasonalCapacityExceptionStatus.ACTIVE -> {
                when (next) {
                    SeasonalCapacityExceptionStatus.SUSPENDED, SeasonalCapacityExceptionStatus.CLOSED -> return true
                }
                return false
            }
            SeasonalCapacityExceptionStatus.SUSPENDED -> return next == SeasonalCapacityExceptionStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: SeasonalCapacityExceptionStatus) {
        if (!canMoveTo(next)) {
            error("seasonal capacity exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun effectiveVolume(): Double = round((distanceKm + weight) * 100) / 100

    fun snapshot(): SeasonalCapacityException {
        return SeasonalCapacityException(
            code = code,
            name = name,
            distanceKm = distanceKm,
            source = source,
            weight = weight,
            timezone = timezone,
            rate = rate,
            currency = currency,
            reviewer = reviewer,
            limit = limit,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a seasonal capacity exception in status draft. */
fun newSeasonalCapacityException(code: String, name: String): SeasonalCapacityException {
    return SeasonalCapacityException(
        code = code,
        name = name,
        distanceKm = 0.0,
        source = "",
        weight = 0.0,
        timezone = "",
        rate = 0.0,
        currency = "",
        reviewer = "",
        limit = 0,
        routeCode = "",
        status = SeasonalCapacityExceptionStatus.DRAFT,
    )
}
