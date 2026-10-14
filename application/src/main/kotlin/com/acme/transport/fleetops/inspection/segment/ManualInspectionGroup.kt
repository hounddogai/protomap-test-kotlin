package com.acme.transport.fleetops.inspection.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual inspection group is in its life cycle. */
enum class ManualInspectionGroupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A manual inspection group. Its code is unique within a tenant. */
data class ManualInspectionGroup(
    var code: String,
    var name: String,
    var pinned: Boolean,
    var loadKg: Double,
    var minimum: Double,
    var status: ManualInspectionGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualInspectionGroupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualInspectionGroupStatus): Boolean {
        when (status) {
            ManualInspectionGroupStatus.PLANNED -> return next == ManualInspectionGroupStatus.CANCELLED
            ManualInspectionGroupStatus.SCHEDULED -> {
                when (next) {
                    ManualInspectionGroupStatus.COMPLETED -> return true
                }
                return false
            }
            ManualInspectionGroupStatus.IN_PROGRESS -> return next == ManualInspectionGroupStatus.COMPLETED
            ManualInspectionGroupStatus.COMPLETED -> return next == ManualInspectionGroupStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: ManualInspectionGroupStatus) {
        if (!canMoveTo(next)) {
            error("manual inspection group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun adjustedTotal(): Double = round((loadKg + minimum) * 100) / 100

    fun snapshot(): ManualInspectionGroup {
        return ManualInspectionGroup(
            code = code,
            name = name,
            pinned = pinned,
            loadKg = loadKg,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a manual inspection group in status planned. */
fun newManualInspectionGroup(code: String, name: String): ManualInspectionGroup {
    return ManualInspectionGroup(
        code = code,
        name = name,
        pinned = false,
        loadKg = 0.0,
        minimum = 0.0,
        status = ManualInspectionGroupStatus.PLANNED,
    )
}
