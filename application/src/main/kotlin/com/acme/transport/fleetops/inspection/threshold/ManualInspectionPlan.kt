package com.acme.transport.fleetops.inspection.threshold

import com.acme.transport.kit.text.normalize

/** Where a manual inspection plan is in its life cycle. */
enum class ManualInspectionPlanStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A manual inspection plan. Its code is unique within a tenant. */
data class ManualInspectionPlan(
    var code: String,
    var label: String,
    var category: String,
    var pinned: Boolean,
    var phone: String,
    var lane: String,
    var score: Double,
    var status: ManualInspectionPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualInspectionPlanStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualInspectionPlanStatus): Boolean {
        when (status) {
            ManualInspectionPlanStatus.CREATED -> {
                when (next) {
                    ManualInspectionPlanStatus.PUBLISHED, ManualInspectionPlanStatus.RETIRED -> return true
                }
                return false
            }
            ManualInspectionPlanStatus.VALIDATED -> return next == ManualInspectionPlanStatus.PUBLISHED
            ManualInspectionPlanStatus.PUBLISHED -> return next == ManualInspectionPlanStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ManualInspectionPlanStatus) {
        if (!canMoveTo(next)) {
            error("manual inspection plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"manual inspection plan"} $code"

    fun measuredValue(): Double = score

    fun snapshot(): ManualInspectionPlan {
        return ManualInspectionPlan(
            code = code,
            label = label,
            category = category,
            pinned = pinned,
            phone = phone,
            lane = lane,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a manual inspection plan in status created. */
fun newManualInspectionPlan(code: String, label: String): ManualInspectionPlan {
    return ManualInspectionPlan(
        code = code,
        label = label,
        category = "",
        pinned = false,
        phone = "",
        lane = "",
        score = 0.0,
        status = ManualInspectionPlanStatus.CREATED,
    )
}
