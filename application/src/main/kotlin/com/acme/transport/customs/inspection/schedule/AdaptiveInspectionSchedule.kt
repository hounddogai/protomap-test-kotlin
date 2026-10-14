package com.acme.transport.customs.inspection.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive inspection schedule records of customs's inspection work, and the rules, stores, and
// calculations around them.

/** Where a adaptive inspection schedule is in its life cycle. */
enum class AdaptiveInspectionScheduleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A adaptive inspection schedule. Its code is unique within a tenant. */
data class AdaptiveInspectionSchedule(
    var code: String,
    var name: String,
    var ownerId: String,
    var cost: Double,
    var endsAt: Int,
    var pallets: Int,
    var status: AdaptiveInspectionScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveInspectionScheduleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveInspectionScheduleStatus): Boolean {
        when (status) {
            AdaptiveInspectionScheduleStatus.PROPOSED -> {
                when (next) {
                    AdaptiveInspectionScheduleStatus.APPROVED, AdaptiveInspectionScheduleStatus.WITHDRAWN -> return true
                }
                return false
            }
            AdaptiveInspectionScheduleStatus.APPROVED -> return next == AdaptiveInspectionScheduleStatus.REJECTED || next == AdaptiveInspectionScheduleStatus.WITHDRAWN
            AdaptiveInspectionScheduleStatus.REJECTED -> {
                when (next) {
                    AdaptiveInspectionScheduleStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveInspectionScheduleStatus) {
        if (!canMoveTo(next)) {
            error("adaptive inspection schedule $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"adaptive inspection schedule"} $code"

    fun adjustedExposure(): Double = round((endsAt.toDouble() + pallets.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveInspectionSchedule {
        return AdaptiveInspectionSchedule(
            code = code,
            name = name,
            ownerId = ownerId,
            cost = cost,
            endsAt = endsAt,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a adaptive inspection schedule in status proposed. */
fun newAdaptiveInspectionSchedule(code: String, name: String): AdaptiveInspectionSchedule {
    return AdaptiveInspectionSchedule(
        code = code,
        name = name,
        ownerId = "",
        cost = 0.0,
        endsAt = 0,
        pallets = 0,
        status = AdaptiveInspectionScheduleStatus.PROPOSED,
    )
}
