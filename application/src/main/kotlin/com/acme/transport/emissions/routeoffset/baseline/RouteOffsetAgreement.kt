package com.acme.transport.emissions.routeoffset.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a route offset agreement is in its life cycle. */
enum class RouteOffsetAgreementStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a route offset agreement and its current status. */
data class RouteOffsetAgreement(
    var code: String,
    var title: String,
    var cost: Double,
    var amount: Double,
    var description: String,
    var graceDays: Int,
    var margin: Double,
    var vehicleId: String,
    var status: RouteOffsetAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetAgreementStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetAgreementStatus): Boolean {
        when (status) {
            RouteOffsetAgreementStatus.CREATED -> return next == RouteOffsetAgreementStatus.VALIDATED || next == RouteOffsetAgreementStatus.RETIRED
            RouteOffsetAgreementStatus.VALIDATED -> return next == RouteOffsetAgreementStatus.PUBLISHED
            RouteOffsetAgreementStatus.PUBLISHED -> return next == RouteOffsetAgreementStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RouteOffsetAgreementStatus) {
        if (!canMoveTo(next)) {
            error("route offset agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun netVolume(): Double = round(graceDays.toDouble() * margin * 100) / 100

    fun snapshot(): RouteOffsetAgreement {
        return RouteOffsetAgreement(
            code = code,
            title = title,
            cost = cost,
            amount = amount,
            description = description,
            graceDays = graceDays,
            margin = margin,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a route offset agreement in status created. */
fun newRouteOffsetAgreement(code: String, title: String): RouteOffsetAgreement {
    return RouteOffsetAgreement(
        code = code,
        title = title,
        cost = 0.0,
        amount = 0.0,
        description = "",
        graceDays = 0,
        margin = 0.0,
        vehicleId = "",
        status = RouteOffsetAgreementStatus.CREATED,
    )
}
