package com.acme.transport.crewing.dutyperiod.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a duty period plan is in its life cycle. */
enum class DutyPeriodPlanStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A duty period plan as the duty period team keeps it. */
data class DutyPeriodPlan(
    var code: String,
    var label: String,
    var segment: String,
    var discount: Double,
    var phone: String,
    var updatedAt: Int,
    var reviewer: String,
    var notes: String,
    var name: String,
    var unit: String,
    var stopCount: Int,
    var status: DutyPeriodPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodPlanStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodPlanStatus): Boolean {
        when (status) {
            DutyPeriodPlanStatus.IDLE -> return next == DutyPeriodPlanStatus.READY
            DutyPeriodPlanStatus.WARMING -> {
                when (next) {
                    DutyPeriodPlanStatus.READY, DutyPeriodPlanStatus.DRAINING -> return true
                }
                return false
            }
            DutyPeriodPlanStatus.READY -> return next == DutyPeriodPlanStatus.STOPPED
            DutyPeriodPlanStatus.DRAINING -> {
                when (next) {
                    DutyPeriodPlanStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DutyPeriodPlanStatus) {
        if (!canMoveTo(next)) {
            error("duty period plan $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveLoad(): Double = round((updatedAt.toDouble() - stopCount.toDouble()) * 100) / 100

    fun snapshot(): DutyPeriodPlan {
        return DutyPeriodPlan(
            code = code,
            label = label,
            segment = segment,
            discount = discount,
            phone = phone,
            updatedAt = updatedAt,
            reviewer = reviewer,
            notes = notes,
            name = name,
            unit = unit,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a duty period plan in status idle. */
fun newDutyPeriodPlan(code: String, label: String): DutyPeriodPlan {
    return DutyPeriodPlan(
        code = code,
        label = label,
        segment = "",
        discount = 0.0,
        phone = "",
        updatedAt = 0,
        reviewer = "",
        notes = "",
        name = "",
        unit = "",
        stopCount = 0,
        status = DutyPeriodPlanStatus.IDLE,
    )
}
