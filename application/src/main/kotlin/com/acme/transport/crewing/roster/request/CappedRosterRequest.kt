package com.acme.transport.crewing.roster.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped roster request records of crewing's roster work, and the rules, stores, and calculations around
// them.

/** Where a capped roster request is in its life cycle. */
enum class CappedRosterRequestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One capped roster request, with what the planners need to act on it. */
data class CappedRosterRequest(
    var code: String,
    var name: String,
    var reviewer: String,
    var budget: Double,
    var endsAt: Int,
    var approved: Boolean,
    var routeCode: String,
    var status: CappedRosterRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedRosterRequestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedRosterRequestStatus): Boolean {
        when (status) {
            CappedRosterRequestStatus.DRAFT -> {
                when (next) {
                    CappedRosterRequestStatus.ACTIVE, CappedRosterRequestStatus.SUSPENDED -> return true
                }
                return false
            }
            CappedRosterRequestStatus.PENDING -> {
                when (next) {
                    CappedRosterRequestStatus.ACTIVE, CappedRosterRequestStatus.SUSPENDED -> return true
                }
                return false
            }
            CappedRosterRequestStatus.ACTIVE -> return next == CappedRosterRequestStatus.SUSPENDED
            CappedRosterRequestStatus.SUSPENDED -> return next == CappedRosterRequestStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: CappedRosterRequestStatus) {
        if (!canMoveTo(next)) {
            error("capped roster request $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun weightedLoad(): Double = round((budget - endsAt.toDouble()) * 100) / 100

    fun snapshot(): CappedRosterRequest {
        return CappedRosterRequest(
            code = code,
            name = name,
            reviewer = reviewer,
            budget = budget,
            endsAt = endsAt,
            approved = approved,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a capped roster request in status draft. */
fun newCappedRosterRequest(code: String, name: String): CappedRosterRequest {
    return CappedRosterRequest(
        code = code,
        name = name,
        reviewer = "",
        budget = 0.0,
        endsAt = 0,
        approved = false,
        routeCode = "",
        status = CappedRosterRequestStatus.DRAFT,
    )
}
