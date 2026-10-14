package com.acme.transport.safety.scorecard.defaultscorecardledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional scorecard threshold is in its life cycle. */
enum class RegionalScorecardThresholdStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a regional scorecard threshold and its current status. */
data class RegionalScorecardThreshold(
    var code: String,
    var name: String,
    var sequence: Int,
    var notes: String,
    var etaMinutes: Int,
    var weight: Double,
    var attempts: Int,
    var stopCount: Int,
    var status: RegionalScorecardThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalScorecardThresholdStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalScorecardThresholdStatus): Boolean {
        when (status) {
            RegionalScorecardThresholdStatus.CREATED -> {
                when (next) {
                    RegionalScorecardThresholdStatus.PUBLISHED, RegionalScorecardThresholdStatus.RETIRED -> return true
                }
                return false
            }
            RegionalScorecardThresholdStatus.VALIDATED -> return next == RegionalScorecardThresholdStatus.PUBLISHED
            RegionalScorecardThresholdStatus.PUBLISHED -> {
                when (next) {
                    RegionalScorecardThresholdStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalScorecardThresholdStatus) {
        if (!canMoveTo(next)) {
            error("regional scorecard threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun adjustedExposure(): Double = round((weight - sequence.toDouble()) * 100) / 100

    fun snapshot(): RegionalScorecardThreshold {
        return RegionalScorecardThreshold(
            code = code,
            name = name,
            sequence = sequence,
            notes = notes,
            etaMinutes = etaMinutes,
            weight = weight,
            attempts = attempts,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional scorecard threshold in status created. */
fun newRegionalScorecardThreshold(code: String, name: String): RegionalScorecardThreshold {
    return RegionalScorecardThreshold(
        code = code,
        name = name,
        sequence = 0,
        notes = "",
        etaMinutes = 0,
        weight = 0.0,
        attempts = 0,
        stopCount = 0,
        status = RegionalScorecardThresholdStatus.CREATED,
    )
}
