package com.acme.transport.customs.inspection.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling inspection milestone is in its life cycle. */
enum class RollingInspectionMilestoneStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A rolling inspection milestone as the inspection team keeps it. */
data class RollingInspectionMilestone(
    var code: String,
    var label: String,
    var archived: Boolean,
    var visible: Boolean,
    var retries: Int,
    var margin: Double,
    var shiftCode: String,
    var capacity: Int,
    var maximum: Double,
    var amount: Double,
    var status: RollingInspectionMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingInspectionMilestoneStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingInspectionMilestoneStatus): Boolean {
        when (status) {
            RollingInspectionMilestoneStatus.CREATED -> return next == RollingInspectionMilestoneStatus.RETIRED
            RollingInspectionMilestoneStatus.VALIDATED -> {
                when (next) {
                    RollingInspectionMilestoneStatus.PUBLISHED, RollingInspectionMilestoneStatus.RETIRED -> return true
                }
                return false
            }
            RollingInspectionMilestoneStatus.PUBLISHED -> return next == RollingInspectionMilestoneStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RollingInspectionMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("rolling inspection milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"rolling inspection milestone"} $code"

    fun effectiveTotal(): Double = round((margin - retries.toDouble()) * 100) / 100

    fun snapshot(): RollingInspectionMilestone {
        return RollingInspectionMilestone(
            code = code,
            label = label,
            archived = archived,
            visible = visible,
            retries = retries,
            margin = margin,
            shiftCode = shiftCode,
            capacity = capacity,
            maximum = maximum,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rolling inspection milestone in status created. */
fun newRollingInspectionMilestone(code: String, label: String): RollingInspectionMilestone {
    return RollingInspectionMilestone(
        code = code,
        label = label,
        archived = false,
        visible = false,
        retries = 0,
        margin = 0.0,
        shiftCode = "",
        capacity = 0,
        maximum = 0.0,
        amount = 0.0,
        status = RollingInspectionMilestoneStatus.CREATED,
    )
}
