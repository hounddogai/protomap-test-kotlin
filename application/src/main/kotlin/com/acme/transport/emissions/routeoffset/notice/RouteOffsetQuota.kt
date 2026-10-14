package com.acme.transport.emissions.routeoffset.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a route offset quota is in its life cycle. */
enum class RouteOffsetQuotaStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a route offset quota and its current status. */
data class RouteOffsetQuota(
    var code: String,
    var label: String,
    var version: Int,
    var floorValue: Double,
    var source: String,
    var durationMinutes: Int,
    var loadKg: Double,
    var endsAt: Int,
    var amount: Double,
    var weight: Double,
    var status: RouteOffsetQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetQuotaStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetQuotaStatus): Boolean {
        when (status) {
            RouteOffsetQuotaStatus.DRAFT -> return next == RouteOffsetQuotaStatus.SUSPENDED || next == RouteOffsetQuotaStatus.CLOSED
            RouteOffsetQuotaStatus.PENDING -> return next == RouteOffsetQuotaStatus.SUSPENDED
            RouteOffsetQuotaStatus.ACTIVE -> return next == RouteOffsetQuotaStatus.SUSPENDED
            RouteOffsetQuotaStatus.SUSPENDED -> return next == RouteOffsetQuotaStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RouteOffsetQuotaStatus) {
        if (!canMoveTo(next)) {
            error("route offset quota $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedValue(): Double = round((durationMinutes.toDouble() + amount) * 100) / 100

    fun snapshot(): RouteOffsetQuota {
        return RouteOffsetQuota(
            code = code,
            label = label,
            version = version,
            floorValue = floorValue,
            source = source,
            durationMinutes = durationMinutes,
            loadKg = loadKg,
            endsAt = endsAt,
            amount = amount,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a route offset quota in status draft. */
fun newRouteOffsetQuota(code: String, label: String): RouteOffsetQuota {
    return RouteOffsetQuota(
        code = code,
        label = label,
        version = 0,
        floorValue = 0.0,
        source = "",
        durationMinutes = 0,
        loadKg = 0.0,
        endsAt = 0,
        amount = 0.0,
        weight = 0.0,
        status = RouteOffsetQuotaStatus.DRAFT,
    )
}
