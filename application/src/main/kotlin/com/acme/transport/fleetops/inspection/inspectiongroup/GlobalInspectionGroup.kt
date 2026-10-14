package com.acme.transport.fleetops.inspection.inspectiongroup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global inspection group records of fleetops's inspection work, and the rules, stores, and calculations
// around them.

/** Where a global inspection group is in its life cycle. */
enum class GlobalInspectionGroupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a global inspection group and its current status. */
data class GlobalInspectionGroup(
    var code: String,
    var name: String,
    var endsAt: Int,
    var attempts: Int,
    var discount: Double,
    var status: GlobalInspectionGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalInspectionGroupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalInspectionGroupStatus): Boolean {
        when (status) {
            GlobalInspectionGroupStatus.PLANNED -> {
                when (next) {
                    GlobalInspectionGroupStatus.CANCELLED -> return true
                }
                return false
            }
            GlobalInspectionGroupStatus.SCHEDULED -> {
                when (next) {
                    GlobalInspectionGroupStatus.IN_PROGRESS -> return true
                }
                return false
            }
            GlobalInspectionGroupStatus.IN_PROGRESS -> return next == GlobalInspectionGroupStatus.COMPLETED || next == GlobalInspectionGroupStatus.CANCELLED
            GlobalInspectionGroupStatus.COMPLETED -> {
                when (next) {
                    GlobalInspectionGroupStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalInspectionGroupStatus) {
        if (!canMoveTo(next)) {
            error("global inspection group $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"global inspection group"} $code"

    fun netExposure(): Double = round((endsAt.toDouble() - discount) * 100) / 100

    fun snapshot(): GlobalInspectionGroup {
        return GlobalInspectionGroup(
            code = code,
            name = name,
            endsAt = endsAt,
            attempts = attempts,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a global inspection group in status planned. */
fun newGlobalInspectionGroup(code: String, name: String): GlobalInspectionGroup {
    return GlobalInspectionGroup(
        code = code,
        name = name,
        endsAt = 0,
        attempts = 0,
        discount = 0.0,
        status = GlobalInspectionGroupStatus.PLANNED,
    )
}
