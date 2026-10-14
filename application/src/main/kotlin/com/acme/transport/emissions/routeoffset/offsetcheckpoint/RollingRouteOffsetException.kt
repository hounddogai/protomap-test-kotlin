package com.acme.transport.emissions.routeoffset.offsetcheckpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling route offset exception is in its life cycle. */
enum class RollingRouteOffsetExceptionStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a rolling route offset exception and its current status. */
data class RollingRouteOffsetException(
    var code: String,
    var title: String,
    var weight: Double,
    var unit: String,
    var updatedAt: Int,
    var rate: Double,
    var odometer: Int,
    var category: String,
    var depot: String,
    var status: RollingRouteOffsetExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingRouteOffsetExceptionStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingRouteOffsetExceptionStatus): Boolean {
        when (status) {
            RollingRouteOffsetExceptionStatus.REQUESTED -> {
                when (next) {
                    RollingRouteOffsetExceptionStatus.REVOKED, RollingRouteOffsetExceptionStatus.EXPIRED -> return true
                }
                return false
            }
            RollingRouteOffsetExceptionStatus.GRANTED -> {
                when (next) {
                    RollingRouteOffsetExceptionStatus.REVOKED, RollingRouteOffsetExceptionStatus.EXPIRED -> return true
                }
                return false
            }
            RollingRouteOffsetExceptionStatus.REVOKED -> return next == RollingRouteOffsetExceptionStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RollingRouteOffsetExceptionStatus) {
        if (!canMoveTo(next)) {
            error("rolling route offset exception $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun weightedExposure(): Double = round((updatedAt.toDouble() - rate) * 100) / 100

    fun snapshot(): RollingRouteOffsetException {
        return RollingRouteOffsetException(
            code = code,
            title = title,
            weight = weight,
            unit = unit,
            updatedAt = updatedAt,
            rate = rate,
            odometer = odometer,
            category = category,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a rolling route offset exception in status requested. */
fun newRollingRouteOffsetException(code: String, title: String): RollingRouteOffsetException {
    return RollingRouteOffsetException(
        code = code,
        title = title,
        weight = 0.0,
        unit = "",
        updatedAt = 0,
        rate = 0.0,
        odometer = 0,
        category = "",
        depot = "",
        status = RollingRouteOffsetExceptionStatus.REQUESTED,
    )
}
