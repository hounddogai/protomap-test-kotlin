package com.acme.transport.fleetops.inspection.inspectionscore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted inspection score records of fleetops's inspection work, and the rules, stores, and
// calculations around them.

/** Where a weighted inspection score is in its life cycle. */
enum class WeightedInspectionScoreStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a weighted inspection score and its current status. */
data class WeightedInspectionScore(
    var code: String,
    var label: String,
    var active: Boolean,
    var lane: String,
    var batchSize: Int,
    var durationMinutes: Int,
    var version: Int,
    var status: WeightedInspectionScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedInspectionScoreStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedInspectionScoreStatus): Boolean {
        when (status) {
            WeightedInspectionScoreStatus.DRAFT -> {
                when (next) {
                    WeightedInspectionScoreStatus.PENDING, WeightedInspectionScoreStatus.ACTIVE -> return true
                }
                return false
            }
            WeightedInspectionScoreStatus.PENDING -> return next == WeightedInspectionScoreStatus.CLOSED
            WeightedInspectionScoreStatus.ACTIVE -> return next == WeightedInspectionScoreStatus.SUSPENDED || next == WeightedInspectionScoreStatus.CLOSED
            WeightedInspectionScoreStatus.SUSPENDED -> return next == WeightedInspectionScoreStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: WeightedInspectionScoreStatus) {
        if (!canMoveTo(next)) {
            error("weighted inspection score $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedExposure(): Double = round((version.toDouble() + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): WeightedInspectionScore {
        return WeightedInspectionScore(
            code = code,
            label = label,
            active = active,
            lane = lane,
            batchSize = batchSize,
            durationMinutes = durationMinutes,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a weighted inspection score in status draft. */
fun newWeightedInspectionScore(code: String, label: String): WeightedInspectionScore {
    return WeightedInspectionScore(
        code = code,
        label = label,
        active = false,
        lane = "",
        batchSize = 0,
        durationMinutes = 0,
        version = 0,
        status = WeightedInspectionScoreStatus.DRAFT,
    )
}
