package com.acme.transport.crewing.roster.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global roster entry records of crewing's roster work, and the rules, stores, and calculations around
// them.

/** Where a global roster entry is in its life cycle. */
enum class GlobalRosterEntryStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A global roster entry as the roster team keeps it. */
data class GlobalRosterEntry(
    var code: String,
    var name: String,
    var discount: Double,
    var floorValue: Double,
    var weight: Double,
    var trailerId: String,
    var currency: String,
    var attempts: Int,
    var externalRef: String,
    var status: GlobalRosterEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalRosterEntryStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalRosterEntryStatus): Boolean {
        when (status) {
            GlobalRosterEntryStatus.PROPOSED -> {
                when (next) {
                    GlobalRosterEntryStatus.REJECTED -> return true
                }
                return false
            }
            GlobalRosterEntryStatus.APPROVED -> return next == GlobalRosterEntryStatus.WITHDRAWN
            GlobalRosterEntryStatus.REJECTED -> {
                when (next) {
                    GlobalRosterEntryStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalRosterEntryStatus) {
        if (!canMoveTo(next)) {
            error("global roster entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun adjustedTotal(): Double = round(floorValue * weight * 100) / 100

    fun snapshot(): GlobalRosterEntry {
        return GlobalRosterEntry(
            code = code,
            name = name,
            discount = discount,
            floorValue = floorValue,
            weight = weight,
            trailerId = trailerId,
            currency = currency,
            attempts = attempts,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a global roster entry in status proposed. */
fun newGlobalRosterEntry(code: String, name: String): GlobalRosterEntry {
    return GlobalRosterEntry(
        code = code,
        name = name,
        discount = 0.0,
        floorValue = 0.0,
        weight = 0.0,
        trailerId = "",
        currency = "",
        attempts = 0,
        externalRef = "",
        status = GlobalRosterEntryStatus.PROPOSED,
    )
}
