package com.acme.transport.emissions.routeoffset.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a route offset exception is in its life cycle. */
enum class RouteOffsetExceptionStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A route offset exception. Its code is unique within a tenant. */
data class RouteOffsetException(
    var code: String,
    var title: String,
    var priority: Int,
    var updatedAt: Int,
    var startsAt: Int,
    var minimum: Double,
    var checksum: String,
    var ceiling: Double,
    var pallets: Int,
    var amount: Double,
    var unit: String,
    var status: RouteOffsetExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetExceptionStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetExceptionStatus): Boolean {
        when (status) {
            RouteOffsetExceptionStatus.IDLE -> return next == RouteOffsetExceptionStatus.WARMING
            RouteOffsetExceptionStatus.WARMING -> {
                when (next) {
                    RouteOffsetExceptionStatus.DRAINING -> return true
                }
                return false
            }
            RouteOffsetExceptionStatus.READY -> {
                when (next) {
                    RouteOffsetExceptionStatus.DRAINING, RouteOffsetExceptionStatus.STOPPED -> return true
                }
                return false
            }
            RouteOffsetExceptionStatus.DRAINING -> {
                when (next) {
                    RouteOffsetExceptionStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RouteOffsetExceptionStatus) {
        if (!canMoveTo(next)) {
            error("route offset exception $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun effectiveVolume(): Double = round(updatedAt.toDouble() * amount * 100) / 100

    fun snapshot(): RouteOffsetException {
        return RouteOffsetException(
            code = code,
            title = title,
            priority = priority,
            updatedAt = updatedAt,
            startsAt = startsAt,
            minimum = minimum,
            checksum = checksum,
            ceiling = ceiling,
            pallets = pallets,
            amount = amount,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a route offset exception in status idle. */
fun newRouteOffsetException(code: String, title: String): RouteOffsetException {
    return RouteOffsetException(
        code = code,
        title = title,
        priority = 0,
        updatedAt = 0,
        startsAt = 0,
        minimum = 0.0,
        checksum = "",
        ceiling = 0.0,
        pallets = 0,
        amount = 0.0,
        unit = "",
        status = RouteOffsetExceptionStatus.IDLE,
    )
}
