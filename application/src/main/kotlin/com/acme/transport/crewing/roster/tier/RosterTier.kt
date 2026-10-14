package com.acme.transport.crewing.roster.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The roster tier records of crewing's roster work, and the rules, stores, and calculations around them.

/** Where a roster tier is in its life cycle. */
enum class RosterTierStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A roster tier as the roster team keeps it. */
data class RosterTier(
    var code: String,
    var title: String,
    var sequence: Int,
    var driverId: String,
    var stopCount: Int,
    var status: RosterTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterTierStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterTierStatus): Boolean {
        when (status) {
            RosterTierStatus.IDLE -> {
                when (next) {
                    RosterTierStatus.READY -> return true
                }
                return false
            }
            RosterTierStatus.WARMING -> return next == RosterTierStatus.READY || next == RosterTierStatus.DRAINING
            RosterTierStatus.READY -> {
                when (next) {
                    RosterTierStatus.STOPPED -> return true
                }
                return false
            }
            RosterTierStatus.DRAINING -> {
                when (next) {
                    RosterTierStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RosterTierStatus) {
        if (!canMoveTo(next)) {
            error("roster tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun effectiveLoad(): Double = round((stopCount.toDouble() + sequence.toDouble()) * 100) / 100

    fun snapshot(): RosterTier {
        return RosterTier(
            code = code,
            title = title,
            sequence = sequence,
            driverId = driverId,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a roster tier in status idle. */
fun newRosterTier(code: String, title: String): RosterTier {
    return RosterTier(
        code = code,
        title = title,
        sequence = 0,
        driverId = "",
        stopCount = 0,
        status = RosterTierStatus.IDLE,
    )
}
