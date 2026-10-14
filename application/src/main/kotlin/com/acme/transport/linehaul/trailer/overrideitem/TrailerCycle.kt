package com.acme.transport.linehaul.trailer.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer cycle is in its life cycle. */
enum class TrailerCycleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One trailer cycle, with what the planners need to act on it. */
data class TrailerCycle(
    var code: String,
    var title: String,
    var pallets: Int,
    var cost: Double,
    var retries: Int,
    var trailerId: String,
    var carrier: String,
    var timezone: String,
    var country: String,
    var status: TrailerCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerCycleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerCycleStatus): Boolean {
        when (status) {
            TrailerCycleStatus.IDLE -> return next == TrailerCycleStatus.DRAINING || next == TrailerCycleStatus.STOPPED
            TrailerCycleStatus.WARMING -> return next == TrailerCycleStatus.READY || next == TrailerCycleStatus.STOPPED
            TrailerCycleStatus.READY -> {
                when (next) {
                    TrailerCycleStatus.DRAINING -> return true
                }
                return false
            }
            TrailerCycleStatus.DRAINING -> {
                when (next) {
                    TrailerCycleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TrailerCycleStatus) {
        if (!canMoveTo(next)) {
            error("trailer cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun effectiveTotal(): Double = round((retries.toDouble() + cost) * 100) / 100

    fun snapshot(): TrailerCycle {
        return TrailerCycle(
            code = code,
            title = title,
            pallets = pallets,
            cost = cost,
            retries = retries,
            trailerId = trailerId,
            carrier = carrier,
            timezone = timezone,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a trailer cycle in status idle. */
fun newTrailerCycle(code: String, title: String): TrailerCycle {
    return TrailerCycle(
        code = code,
        title = title,
        pallets = 0,
        cost = 0.0,
        retries = 0,
        trailerId = "",
        carrier = "",
        timezone = "",
        country = "",
        status = TrailerCycleStatus.IDLE,
    )
}
