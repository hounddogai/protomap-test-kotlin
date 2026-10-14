package com.acme.transport.linehaul.laneplan.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lane plan entry records of linehaul's lane plan work, and the rules, stores, and calculations around
// them.

/** Where a lane plan entry is in its life cycle. */
enum class LanePlanEntryStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A lane plan entry as the lane plan team keeps it. */
data class LanePlanEntry(
    var code: String,
    var name: String,
    var checksum: String,
    var stopCount: Int,
    var trailerId: String,
    var maximum: Double,
    var currency: String,
    var label: String,
    var sequence: Int,
    var status: LanePlanEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanEntryStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanEntryStatus): Boolean {
        when (status) {
            LanePlanEntryStatus.IDLE -> return next == LanePlanEntryStatus.WARMING
            LanePlanEntryStatus.WARMING -> return next == LanePlanEntryStatus.READY
            LanePlanEntryStatus.READY -> return next == LanePlanEntryStatus.DRAINING || next == LanePlanEntryStatus.STOPPED
            LanePlanEntryStatus.DRAINING -> {
                when (next) {
                    LanePlanEntryStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LanePlanEntryStatus) {
        if (!canMoveTo(next)) {
            error("lane plan entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$name]"

    fun projectedTotal(): Double = round((sequence.toDouble() - maximum) * 100) / 100

    fun snapshot(): LanePlanEntry {
        return LanePlanEntry(
            code = code,
            name = name,
            checksum = checksum,
            stopCount = stopCount,
            trailerId = trailerId,
            maximum = maximum,
            currency = currency,
            label = label,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lane plan entry in status idle. */
fun newLanePlanEntry(code: String, name: String): LanePlanEntry {
    return LanePlanEntry(
        code = code,
        name = name,
        checksum = "",
        stopCount = 0,
        trailerId = "",
        maximum = 0.0,
        currency = "",
        label = "",
        sequence = 0,
        status = LanePlanEntryStatus.IDLE,
    )
}
