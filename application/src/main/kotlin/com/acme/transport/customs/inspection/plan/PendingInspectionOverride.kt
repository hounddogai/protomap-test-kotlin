package com.acme.transport.customs.inspection.plan

import com.acme.transport.kit.text.normalize

/** Where a pending inspection override is in its life cycle. */
enum class PendingInspectionOverrideStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A pending inspection override as the inspection team keeps it. */
data class PendingInspectionOverride(
    var code: String,
    var title: String,
    var segment: String,
    var retries: Int,
    var name: String,
    var status: PendingInspectionOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingInspectionOverrideStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingInspectionOverrideStatus): Boolean {
        when (status) {
            PendingInspectionOverrideStatus.PLANNED -> return next == PendingInspectionOverrideStatus.SCHEDULED || next == PendingInspectionOverrideStatus.CANCELLED
            PendingInspectionOverrideStatus.SCHEDULED -> {
                when (next) {
                    PendingInspectionOverrideStatus.IN_PROGRESS -> return true
                }
                return false
            }
            PendingInspectionOverrideStatus.IN_PROGRESS -> return next == PendingInspectionOverrideStatus.CANCELLED
            PendingInspectionOverrideStatus.COMPLETED -> return next == PendingInspectionOverrideStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: PendingInspectionOverrideStatus) {
        if (!canMoveTo(next)) {
            error("pending inspection override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"pending inspection override"} $code"

    fun measuredValue(): Double = retries.toDouble()

    fun snapshot(): PendingInspectionOverride {
        return PendingInspectionOverride(
            code = code,
            title = title,
            segment = segment,
            retries = retries,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a pending inspection override in status planned. */
fun newPendingInspectionOverride(code: String, title: String): PendingInspectionOverride {
    return PendingInspectionOverride(
        code = code,
        title = title,
        segment = "",
        retries = 0,
        name = "",
        status = PendingInspectionOverrideStatus.PLANNED,
    )
}
