package com.acme.transport.customs.inspection.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a inspection schedule is in its life cycle. */
enum class InspectionScheduleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A inspection schedule as the inspection team keeps it. */
data class InspectionSchedule(
    var code: String,
    var label: String,
    var tags: MutableList<String>,
    var weight: Double,
    var budget: Double,
    var depot: String,
    var durationMinutes: Int,
    var score: Double,
    var sequence: Int,
    var amount: Double,
    var status: InspectionScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionScheduleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionScheduleStatus): Boolean {
        when (status) {
            InspectionScheduleStatus.DRAFT -> {
                when (next) {
                    InspectionScheduleStatus.PENDING, InspectionScheduleStatus.SUSPENDED -> return true
                }
                return false
            }
            InspectionScheduleStatus.PENDING -> {
                when (next) {
                    InspectionScheduleStatus.SUSPENDED, InspectionScheduleStatus.CLOSED -> return true
                }
                return false
            }
            InspectionScheduleStatus.ACTIVE -> return next == InspectionScheduleStatus.SUSPENDED || next == InspectionScheduleStatus.CLOSED
            InspectionScheduleStatus.SUSPENDED -> return next == InspectionScheduleStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: InspectionScheduleStatus) {
        if (!canMoveTo(next)) {
            error("inspection schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedTotal(): Double = round(budget * weight * 100) / 100

    fun snapshot(): InspectionSchedule {
        return InspectionSchedule(
            code = code,
            label = label,
            tags = tags,
            weight = weight,
            budget = budget,
            depot = depot,
            durationMinutes = durationMinutes,
            score = score,
            sequence = sequence,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a inspection schedule in status draft. */
fun newInspectionSchedule(code: String, label: String): InspectionSchedule {
    return InspectionSchedule(
        code = code,
        label = label,
        tags = mutableListOf<String>(),
        weight = 0.0,
        budget = 0.0,
        depot = "",
        durationMinutes = 0,
        score = 0.0,
        sequence = 0,
        amount = 0.0,
        status = InspectionScheduleStatus.DRAFT,
    )
}
