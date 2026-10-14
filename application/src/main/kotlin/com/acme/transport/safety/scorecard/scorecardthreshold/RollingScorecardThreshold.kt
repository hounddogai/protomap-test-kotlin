package com.acme.transport.safety.scorecard.scorecardthreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling scorecard threshold records of safety's scorecard work, and the rules, stores, and calculations
// around them.

/** Where a rolling scorecard threshold is in its life cycle. */
enum class RollingScorecardThresholdStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A rolling scorecard threshold as the scorecard team keeps it. */
data class RollingScorecardThreshold(
    var code: String,
    var label: String,
    var lane: String,
    var driverId: String,
    var quantity: Int,
    var shiftCode: String,
    var rate: Double,
    var timezone: String,
    var status: RollingScorecardThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingScorecardThresholdStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingScorecardThresholdStatus): Boolean {
        when (status) {
            RollingScorecardThresholdStatus.OPEN -> return next == RollingScorecardThresholdStatus.IN_REVIEW || next == RollingScorecardThresholdStatus.ARCHIVED
            RollingScorecardThresholdStatus.IN_REVIEW -> {
                when (next) {
                    RollingScorecardThresholdStatus.REOPENED, RollingScorecardThresholdStatus.ARCHIVED -> return true
                }
                return false
            }
            RollingScorecardThresholdStatus.RESOLVED -> return next == RollingScorecardThresholdStatus.REOPENED
            RollingScorecardThresholdStatus.REOPENED -> {
                when (next) {
                    RollingScorecardThresholdStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingScorecardThresholdStatus) {
        if (!canMoveTo(next)) {
            error("rolling scorecard threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedTotal(): Double = round((rate + quantity.toDouble()) * 100) / 100

    fun snapshot(): RollingScorecardThreshold {
        return RollingScorecardThreshold(
            code = code,
            label = label,
            lane = lane,
            driverId = driverId,
            quantity = quantity,
            shiftCode = shiftCode,
            rate = rate,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a rolling scorecard threshold in status open. */
fun newRollingScorecardThreshold(code: String, label: String): RollingScorecardThreshold {
    return RollingScorecardThreshold(
        code = code,
        label = label,
        lane = "",
        driverId = "",
        quantity = 0,
        shiftCode = "",
        rate = 0.0,
        timezone = "",
        status = RollingScorecardThresholdStatus.OPEN,
    )
}
