package com.acme.transport.safety.scorecard.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling scorecard group records of safety's scorecard work, and the rules, stores, and calculations
// around them.

/** Where a rolling scorecard group is in its life cycle. */
enum class RollingScorecardGroupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One rolling scorecard group, with what the planners need to act on it. */
data class RollingScorecardGroup(
    var code: String,
    var title: String,
    var carrier: String,
    var locale: String,
    var cost: Double,
    var weight: Double,
    var channel: String,
    var notes: String,
    var status: RollingScorecardGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingScorecardGroupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingScorecardGroupStatus): Boolean {
        when (status) {
            RollingScorecardGroupStatus.REQUESTED -> return next == RollingScorecardGroupStatus.EXPIRED
            RollingScorecardGroupStatus.GRANTED -> {
                when (next) {
                    RollingScorecardGroupStatus.EXPIRED -> return true
                }
                return false
            }
            RollingScorecardGroupStatus.REVOKED -> {
                when (next) {
                    RollingScorecardGroupStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingScorecardGroupStatus) {
        if (!canMoveTo(next)) {
            error("rolling scorecard group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun netExposure(): Double = round((weight - cost) * 100) / 100

    fun snapshot(): RollingScorecardGroup {
        return RollingScorecardGroup(
            code = code,
            title = title,
            carrier = carrier,
            locale = locale,
            cost = cost,
            weight = weight,
            channel = channel,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a rolling scorecard group in status requested. */
fun newRollingScorecardGroup(code: String, title: String): RollingScorecardGroup {
    return RollingScorecardGroup(
        code = code,
        title = title,
        carrier = "",
        locale = "",
        cost = 0.0,
        weight = 0.0,
        channel = "",
        notes = "",
        status = RollingScorecardGroupStatus.REQUESTED,
    )
}
