package com.acme.transport.fleetops.leasereturn.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped lease return schedule is in its life cycle. */
enum class CappedLeaseReturnScheduleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One capped lease return schedule, with what the planners need to act on it. */
data class CappedLeaseReturnSchedule(
    var code: String,
    var label: String,
    var version: Int,
    var quantity: Int,
    var notes: String,
    var sequence: Int,
    var distanceKm: Double,
    var status: CappedLeaseReturnScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedLeaseReturnScheduleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedLeaseReturnScheduleStatus): Boolean {
        when (status) {
            CappedLeaseReturnScheduleStatus.OPEN -> return next == CappedLeaseReturnScheduleStatus.IN_REVIEW || next == CappedLeaseReturnScheduleStatus.RESOLVED
            CappedLeaseReturnScheduleStatus.IN_REVIEW -> return next == CappedLeaseReturnScheduleStatus.REOPENED || next == CappedLeaseReturnScheduleStatus.ARCHIVED
            CappedLeaseReturnScheduleStatus.RESOLVED -> return next == CappedLeaseReturnScheduleStatus.REOPENED
            CappedLeaseReturnScheduleStatus.REOPENED -> return next == CappedLeaseReturnScheduleStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: CappedLeaseReturnScheduleStatus) {
        if (!canMoveTo(next)) {
            error("capped lease return schedule $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "${"capped lease return schedule"} $code"

    fun projectedValue(): Double = round((distanceKm - quantity.toDouble()) * 100) / 100

    fun snapshot(): CappedLeaseReturnSchedule {
        return CappedLeaseReturnSchedule(
            code = code,
            label = label,
            version = version,
            quantity = quantity,
            notes = notes,
            sequence = sequence,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capped lease return schedule in status open. */
fun newCappedLeaseReturnSchedule(code: String, label: String): CappedLeaseReturnSchedule {
    return CappedLeaseReturnSchedule(
        code = code,
        label = label,
        version = 0,
        quantity = 0,
        notes = "",
        sequence = 0,
        distanceKm = 0.0,
        status = CappedLeaseReturnScheduleStatus.OPEN,
    )
}
