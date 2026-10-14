package com.acme.transport.emissions.routeoffset.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a route offset review is in its life cycle. */
enum class RouteOffsetReviewStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A route offset review. Its code is unique within a tenant. */
data class RouteOffsetReview(
    var code: String,
    var name: String,
    var maximum: Double,
    var loadKg: Double,
    var country: String,
    var status: RouteOffsetReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetReviewStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetReviewStatus): Boolean {
        when (status) {
            RouteOffsetReviewStatus.PROPOSED -> return next == RouteOffsetReviewStatus.APPROVED || next == RouteOffsetReviewStatus.WITHDRAWN
            RouteOffsetReviewStatus.APPROVED -> return next == RouteOffsetReviewStatus.REJECTED || next == RouteOffsetReviewStatus.WITHDRAWN
            RouteOffsetReviewStatus.REJECTED -> return next == RouteOffsetReviewStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: RouteOffsetReviewStatus) {
        if (!canMoveTo(next)) {
            error("route offset review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun projectedTotal(): Double = round((maximum - loadKg) * 100) / 100

    fun snapshot(): RouteOffsetReview {
        return RouteOffsetReview(
            code = code,
            name = name,
            maximum = maximum,
            loadKg = loadKg,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a route offset review in status proposed. */
fun newRouteOffsetReview(code: String, name: String): RouteOffsetReview {
    return RouteOffsetReview(
        code = code,
        name = name,
        maximum = 0.0,
        loadKg = 0.0,
        country = "",
        status = RouteOffsetReviewStatus.PROPOSED,
    )
}
