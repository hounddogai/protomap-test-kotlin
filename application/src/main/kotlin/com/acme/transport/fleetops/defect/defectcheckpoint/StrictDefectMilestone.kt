package com.acme.transport.fleetops.defect.defectcheckpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict defect milestone is in its life cycle. */
enum class StrictDefectMilestoneStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A strict defect milestone. Its code is unique within a tenant. */
data class StrictDefectMilestone(
    var code: String,
    var label: String,
    var updatedAt: Int,
    var notes: String,
    var depot: String,
    var expiresAt: Int,
    var status: StrictDefectMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictDefectMilestoneStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictDefectMilestoneStatus): Boolean {
        when (status) {
            StrictDefectMilestoneStatus.CREATED -> {
                when (next) {
                    StrictDefectMilestoneStatus.VALIDATED, StrictDefectMilestoneStatus.PUBLISHED -> return true
                }
                return false
            }
            StrictDefectMilestoneStatus.VALIDATED -> return next == StrictDefectMilestoneStatus.PUBLISHED || next == StrictDefectMilestoneStatus.RETIRED
            StrictDefectMilestoneStatus.PUBLISHED -> return next == StrictDefectMilestoneStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: StrictDefectMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("strict defect milestone $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedValue(): Double = round(updatedAt.toDouble() * expiresAt.toDouble() * 100) / 100

    fun snapshot(): StrictDefectMilestone {
        return StrictDefectMilestone(
            code = code,
            label = label,
            updatedAt = updatedAt,
            notes = notes,
            depot = depot,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a strict defect milestone in status created. */
fun newStrictDefectMilestone(code: String, label: String): StrictDefectMilestone {
    return StrictDefectMilestone(
        code = code,
        label = label,
        updatedAt = 0,
        notes = "",
        depot = "",
        expiresAt = 0,
        status = StrictDefectMilestoneStatus.CREATED,
    )
}
