package com.acme.transport.crewing.restbreak.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rest break entry records of crewing's rest break work, and the rules, stores, and calculations around
// them.

/** Where a rest break entry is in its life cycle. */
enum class RestBreakEntryStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A rest break entry as the rest break team keeps it. */
data class RestBreakEntry(
    var code: String,
    var name: String,
    var lane: String,
    var retries: Int,
    var currency: String,
    var score: Double,
    var trailerId: String,
    var depot: String,
    var vehicleId: String,
    var status: RestBreakEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakEntryStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakEntryStatus): Boolean {
        when (status) {
            RestBreakEntryStatus.DRAFT -> return next == RestBreakEntryStatus.ACTIVE || next == RestBreakEntryStatus.CLOSED
            RestBreakEntryStatus.PENDING -> return next == RestBreakEntryStatus.ACTIVE || next == RestBreakEntryStatus.CLOSED
            RestBreakEntryStatus.ACTIVE -> return next == RestBreakEntryStatus.CLOSED
            RestBreakEntryStatus.SUSPENDED -> {
                when (next) {
                    RestBreakEntryStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RestBreakEntryStatus) {
        if (!canMoveTo(next)) {
            error("rest break entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun adjustedLoad(): Double = round(score * retries.toDouble() * 100) / 100

    fun snapshot(): RestBreakEntry {
        return RestBreakEntry(
            code = code,
            name = name,
            lane = lane,
            retries = retries,
            currency = currency,
            score = score,
            trailerId = trailerId,
            depot = depot,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a rest break entry in status draft. */
fun newRestBreakEntry(code: String, name: String): RestBreakEntry {
    return RestBreakEntry(
        code = code,
        name = name,
        lane = "",
        retries = 0,
        currency = "",
        score = 0.0,
        trailerId = "",
        depot = "",
        vehicleId = "",
        status = RestBreakEntryStatus.DRAFT,
    )
}
