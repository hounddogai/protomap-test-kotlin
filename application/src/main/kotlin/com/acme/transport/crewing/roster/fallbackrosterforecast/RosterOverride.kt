package com.acme.transport.crewing.roster.fallbackrosterforecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a roster override is in its life cycle. */
enum class RosterOverrideStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A roster override as the roster team keeps it. */
data class RosterOverride(
    var code: String,
    var label: String,
    var trailerId: String,
    var limit: Int,
    var cost: Double,
    var quantity: Int,
    var title: String,
    var updatedAt: Int,
    var ceiling: Double,
    var graceDays: Int,
    var status: RosterOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterOverrideStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterOverrideStatus): Boolean {
        when (status) {
            RosterOverrideStatus.REQUESTED -> return next == RosterOverrideStatus.GRANTED || next == RosterOverrideStatus.REVOKED
            RosterOverrideStatus.GRANTED -> return next == RosterOverrideStatus.EXPIRED
            RosterOverrideStatus.REVOKED -> return next == RosterOverrideStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RosterOverrideStatus) {
        if (!canMoveTo(next)) {
            error("roster override $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"roster override"} $code"

    fun netTotal(): Double = round((graceDays.toDouble() + cost) * 100) / 100

    fun snapshot(): RosterOverride {
        return RosterOverride(
            code = code,
            label = label,
            trailerId = trailerId,
            limit = limit,
            cost = cost,
            quantity = quantity,
            title = title,
            updatedAt = updatedAt,
            ceiling = ceiling,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a roster override in status requested. */
fun newRosterOverride(code: String, label: String): RosterOverride {
    return RosterOverride(
        code = code,
        label = label,
        trailerId = "",
        limit = 0,
        cost = 0.0,
        quantity = 0,
        title = "",
        updatedAt = 0,
        ceiling = 0.0,
        graceDays = 0,
        status = RosterOverrideStatus.REQUESTED,
    )
}
