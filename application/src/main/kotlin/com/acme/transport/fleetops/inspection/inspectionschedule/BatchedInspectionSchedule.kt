package com.acme.transport.fleetops.inspection.inspectionschedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched inspection schedule records of fleetops's inspection work, and the rules, stores, and
// calculations around them.

/** Where a batched inspection schedule is in its life cycle. */
enum class BatchedInspectionScheduleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One batched inspection schedule, with what the planners need to act on it. */
data class BatchedInspectionSchedule(
    var code: String,
    var title: String,
    var expiresAt: Int,
    var country: String,
    var loadKg: Double,
    var status: BatchedInspectionScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedInspectionScheduleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedInspectionScheduleStatus): Boolean {
        when (status) {
            BatchedInspectionScheduleStatus.PLANNED -> {
                when (next) {
                    BatchedInspectionScheduleStatus.SCHEDULED -> return true
                }
                return false
            }
            BatchedInspectionScheduleStatus.SCHEDULED -> return next == BatchedInspectionScheduleStatus.CANCELLED
            BatchedInspectionScheduleStatus.IN_PROGRESS -> return next == BatchedInspectionScheduleStatus.COMPLETED || next == BatchedInspectionScheduleStatus.CANCELLED
            BatchedInspectionScheduleStatus.COMPLETED -> return next == BatchedInspectionScheduleStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: BatchedInspectionScheduleStatus) {
        if (!canMoveTo(next)) {
            error("batched inspection schedule $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"batched inspection schedule"} $code"

    fun projectedTotal(): Double = round(loadKg * expiresAt.toDouble() * 100) / 100

    fun snapshot(): BatchedInspectionSchedule {
        return BatchedInspectionSchedule(
            code = code,
            title = title,
            expiresAt = expiresAt,
            country = country,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a batched inspection schedule in status planned. */
fun newBatchedInspectionSchedule(code: String, title: String): BatchedInspectionSchedule {
    return BatchedInspectionSchedule(
        code = code,
        title = title,
        expiresAt = 0,
        country = "",
        loadKg = 0.0,
        status = BatchedInspectionScheduleStatus.PLANNED,
    )
}
