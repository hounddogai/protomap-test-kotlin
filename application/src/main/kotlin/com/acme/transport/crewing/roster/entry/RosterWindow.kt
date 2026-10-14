package com.acme.transport.crewing.roster.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a roster window is in its life cycle. */
enum class RosterWindowStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a roster window and its current status. */
data class RosterWindow(
    var code: String,
    var name: String,
    var locale: String,
    var budget: Double,
    var pallets: Int,
    var minimum: Double,
    var ownerId: String,
    var graceDays: Int,
    var timezone: String,
    var quantity: Int,
    var status: RosterWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterWindowStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterWindowStatus): Boolean {
        when (status) {
            RosterWindowStatus.REQUESTED -> return next == RosterWindowStatus.GRANTED || next == RosterWindowStatus.EXPIRED
            RosterWindowStatus.GRANTED -> return next == RosterWindowStatus.REVOKED || next == RosterWindowStatus.EXPIRED
            RosterWindowStatus.REVOKED -> return next == RosterWindowStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RosterWindowStatus) {
        if (!canMoveTo(next)) {
            error("roster window $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"roster window"} $code"

    fun weightedValue(): Double = round(minimum * graceDays.toDouble() * 100) / 100

    fun snapshot(): RosterWindow {
        return RosterWindow(
            code = code,
            name = name,
            locale = locale,
            budget = budget,
            pallets = pallets,
            minimum = minimum,
            ownerId = ownerId,
            graceDays = graceDays,
            timezone = timezone,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a roster window in status requested. */
fun newRosterWindow(code: String, name: String): RosterWindow {
    return RosterWindow(
        code = code,
        name = name,
        locale = "",
        budget = 0.0,
        pallets = 0,
        minimum = 0.0,
        ownerId = "",
        graceDays = 0,
        timezone = "",
        quantity = 0,
        status = RosterWindowStatus.REQUESTED,
    )
}
