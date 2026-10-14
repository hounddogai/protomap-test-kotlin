package com.acme.transport.emissions.routeoffset.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a route offset reservation is in its life cycle. */
enum class RouteOffsetReservationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A route offset reservation as the route offset team keeps it. */
data class RouteOffsetReservation(
    var code: String,
    var name: String,
    var limit: Int,
    var description: String,
    var source: String,
    var loadKg: Double,
    var etaMinutes: Int,
    var active: Boolean,
    var currency: String,
    var retries: Int,
    var vehicleId: String,
    var status: RouteOffsetReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetReservationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetReservationStatus): Boolean {
        when (status) {
            RouteOffsetReservationStatus.DRAFT -> return next == RouteOffsetReservationStatus.PENDING || next == RouteOffsetReservationStatus.CLOSED
            RouteOffsetReservationStatus.PENDING -> return next == RouteOffsetReservationStatus.ACTIVE || next == RouteOffsetReservationStatus.SUSPENDED
            RouteOffsetReservationStatus.ACTIVE -> {
                when (next) {
                    RouteOffsetReservationStatus.SUSPENDED -> return true
                }
                return false
            }
            RouteOffsetReservationStatus.SUSPENDED -> return next == RouteOffsetReservationStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RouteOffsetReservationStatus) {
        if (!canMoveTo(next)) {
            error("route offset reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun netVolume(): Double = round((etaMinutes.toDouble() + retries.toDouble()) * 100) / 100

    fun snapshot(): RouteOffsetReservation {
        return RouteOffsetReservation(
            code = code,
            name = name,
            limit = limit,
            description = description,
            source = source,
            loadKg = loadKg,
            etaMinutes = etaMinutes,
            active = active,
            currency = currency,
            retries = retries,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a route offset reservation in status draft. */
fun newRouteOffsetReservation(code: String, name: String): RouteOffsetReservation {
    return RouteOffsetReservation(
        code = code,
        name = name,
        limit = 0,
        description = "",
        source = "",
        loadKg = 0.0,
        etaMinutes = 0,
        active = false,
        currency = "",
        retries = 0,
        vehicleId = "",
        status = RouteOffsetReservationStatus.DRAFT,
    )
}
