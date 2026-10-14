package com.acme.transport.safety.scorecard.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual scorecard group is in its life cycle. */
enum class ManualScorecardGroupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A manual scorecard group. Its code is unique within a tenant. */
data class ManualScorecardGroup(
    var code: String,
    var name: String,
    var summary: String,
    var weight: Double,
    var retries: Int,
    var cost: Double,
    var score: Double,
    var status: ManualScorecardGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualScorecardGroupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualScorecardGroupStatus): Boolean {
        when (status) {
            ManualScorecardGroupStatus.PLANNED -> return next == ManualScorecardGroupStatus.CANCELLED
            ManualScorecardGroupStatus.SCHEDULED -> {
                when (next) {
                    ManualScorecardGroupStatus.COMPLETED, ManualScorecardGroupStatus.CANCELLED -> return true
                }
                return false
            }
            ManualScorecardGroupStatus.IN_PROGRESS -> {
                when (next) {
                    ManualScorecardGroupStatus.COMPLETED, ManualScorecardGroupStatus.CANCELLED -> return true
                }
                return false
            }
            ManualScorecardGroupStatus.COMPLETED -> return next == ManualScorecardGroupStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: ManualScorecardGroupStatus) {
        if (!canMoveTo(next)) {
            error("manual scorecard group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun effectiveVolume(): Double = round((retries.toDouble() + cost) * 100) / 100

    fun snapshot(): ManualScorecardGroup {
        return ManualScorecardGroup(
            code = code,
            name = name,
            summary = summary,
            weight = weight,
            retries = retries,
            cost = cost,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a manual scorecard group in status planned. */
fun newManualScorecardGroup(code: String, name: String): ManualScorecardGroup {
    return ManualScorecardGroup(
        code = code,
        name = name,
        summary = "",
        weight = 0.0,
        retries = 0,
        cost = 0.0,
        score = 0.0,
        status = ManualScorecardGroupStatus.PLANNED,
    )
}
