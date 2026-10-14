package com.acme.transport.emissions.routeoffset.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a route offset ledger is in its life cycle. */
enum class RouteOffsetLedgerStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A route offset ledger. Its code is unique within a tenant. */
data class RouteOffsetLedger(
    var code: String,
    var title: String,
    var channel: String,
    var routeCode: String,
    var reviewer: String,
    var loadKg: Double,
    var durationMinutes: Int,
    var status: RouteOffsetLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetLedgerStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetLedgerStatus): Boolean {
        when (status) {
            RouteOffsetLedgerStatus.OPEN -> return next == RouteOffsetLedgerStatus.IN_REVIEW || next == RouteOffsetLedgerStatus.REOPENED
            RouteOffsetLedgerStatus.IN_REVIEW -> {
                when (next) {
                    RouteOffsetLedgerStatus.RESOLVED, RouteOffsetLedgerStatus.ARCHIVED -> return true
                }
                return false
            }
            RouteOffsetLedgerStatus.RESOLVED -> {
                when (next) {
                    RouteOffsetLedgerStatus.REOPENED -> return true
                }
                return false
            }
            RouteOffsetLedgerStatus.REOPENED -> return next == RouteOffsetLedgerStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RouteOffsetLedgerStatus) {
        if (!canMoveTo(next)) {
            error("route offset ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun weightedExposure(): Double = round((loadKg + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): RouteOffsetLedger {
        return RouteOffsetLedger(
            code = code,
            title = title,
            channel = channel,
            routeCode = routeCode,
            reviewer = reviewer,
            loadKg = loadKg,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a route offset ledger in status open. */
fun newRouteOffsetLedger(code: String, title: String): RouteOffsetLedger {
    return RouteOffsetLedger(
        code = code,
        title = title,
        channel = "",
        routeCode = "",
        reviewer = "",
        loadKg = 0.0,
        durationMinutes = 0,
        status = RouteOffsetLedgerStatus.OPEN,
    )
}
