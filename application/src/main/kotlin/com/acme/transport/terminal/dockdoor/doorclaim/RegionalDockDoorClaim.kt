package com.acme.transport.terminal.dockdoor.doorclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional dock door claim records of terminal's dock door work, and the rules, stores, and calculations
// around them.

/** Where a regional dock door claim is in its life cycle. */
enum class RegionalDockDoorClaimStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a regional dock door claim and its current status. */
data class RegionalDockDoorClaim(
    var code: String,
    var name: String,
    var retries: Int,
    var pinned: Boolean,
    var quantity: Int,
    var timezone: String,
    var notes: String,
    var graceDays: Int,
    var routeCode: String,
    var lane: String,
    var expiresAt: Int,
    var status: RegionalDockDoorClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalDockDoorClaimStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalDockDoorClaimStatus): Boolean {
        when (status) {
            RegionalDockDoorClaimStatus.CREATED -> return next == RegionalDockDoorClaimStatus.VALIDATED || next == RegionalDockDoorClaimStatus.RETIRED
            RegionalDockDoorClaimStatus.VALIDATED -> return next == RegionalDockDoorClaimStatus.RETIRED
            RegionalDockDoorClaimStatus.PUBLISHED -> return next == RegionalDockDoorClaimStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RegionalDockDoorClaimStatus) {
        if (!canMoveTo(next)) {
            error("regional dock door claim $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun projectedValue(): Double = round((expiresAt.toDouble() - retries.toDouble()) * 100) / 100

    fun snapshot(): RegionalDockDoorClaim {
        return RegionalDockDoorClaim(
            code = code,
            name = name,
            retries = retries,
            pinned = pinned,
            quantity = quantity,
            timezone = timezone,
            notes = notes,
            graceDays = graceDays,
            routeCode = routeCode,
            lane = lane,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a regional dock door claim in status created. */
fun newRegionalDockDoorClaim(code: String, name: String): RegionalDockDoorClaim {
    return RegionalDockDoorClaim(
        code = code,
        name = name,
        retries = 0,
        pinned = false,
        quantity = 0,
        timezone = "",
        notes = "",
        graceDays = 0,
        routeCode = "",
        lane = "",
        expiresAt = 0,
        status = RegionalDockDoorClaimStatus.CREATED,
    )
}
