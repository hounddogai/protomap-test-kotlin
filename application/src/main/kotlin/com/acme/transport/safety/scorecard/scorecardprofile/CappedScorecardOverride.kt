package com.acme.transport.safety.scorecard.scorecardprofile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped scorecard override is in its life cycle. */
enum class CappedScorecardOverrideStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a capped scorecard override and its current status. */
data class CappedScorecardOverride(
    var code: String,
    var label: String,
    var cost: Double,
    var margin: Double,
    var score: Double,
    var carrier: String,
    var discount: Double,
    var durationMinutes: Int,
    var externalRef: String,
    var status: CappedScorecardOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedScorecardOverrideStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedScorecardOverrideStatus): Boolean {
        when (status) {
            CappedScorecardOverrideStatus.NEW -> {
                when (next) {
                    CappedScorecardOverrideStatus.RUNNING -> return true
                }
                return false
            }
            CappedScorecardOverrideStatus.QUEUED -> return next == CappedScorecardOverrideStatus.RUNNING || next == CappedScorecardOverrideStatus.FAILED
            CappedScorecardOverrideStatus.RUNNING -> {
                when (next) {
                    CappedScorecardOverrideStatus.DONE, CappedScorecardOverrideStatus.FAILED -> return true
                }
                return false
            }
            CappedScorecardOverrideStatus.DONE -> {
                when (next) {
                    CappedScorecardOverrideStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedScorecardOverrideStatus) {
        if (!canMoveTo(next)) {
            error("capped scorecard override $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun netTotal(): Double = round((margin - score) * 100) / 100

    fun snapshot(): CappedScorecardOverride {
        return CappedScorecardOverride(
            code = code,
            label = label,
            cost = cost,
            margin = margin,
            score = score,
            carrier = carrier,
            discount = discount,
            durationMinutes = durationMinutes,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capped scorecard override in status new. */
fun newCappedScorecardOverride(code: String, label: String): CappedScorecardOverride {
    return CappedScorecardOverride(
        code = code,
        label = label,
        cost = 0.0,
        margin = 0.0,
        score = 0.0,
        carrier = "",
        discount = 0.0,
        durationMinutes = 0,
        externalRef = "",
        status = CappedScorecardOverrideStatus.NEW,
    )
}
