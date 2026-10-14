package com.acme.transport.fleetops.inspection.inspectionbatch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending inspection slot is in its life cycle. */
enum class PendingInspectionSlotStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One pending inspection slot, with what the planners need to act on it. */
data class PendingInspectionSlot(
    var code: String,
    var name: String,
    var routeCode: String,
    var limit: Int,
    var windowDays: Int,
    var category: String,
    var shiftCode: String,
    var status: PendingInspectionSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingInspectionSlotStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingInspectionSlotStatus): Boolean {
        when (status) {
            PendingInspectionSlotStatus.PLANNED -> {
                when (next) {
                    PendingInspectionSlotStatus.COMPLETED, PendingInspectionSlotStatus.CANCELLED -> return true
                }
                return false
            }
            PendingInspectionSlotStatus.SCHEDULED -> {
                when (next) {
                    PendingInspectionSlotStatus.IN_PROGRESS, PendingInspectionSlotStatus.CANCELLED -> return true
                }
                return false
            }
            PendingInspectionSlotStatus.IN_PROGRESS -> {
                when (next) {
                    PendingInspectionSlotStatus.COMPLETED, PendingInspectionSlotStatus.CANCELLED -> return true
                }
                return false
            }
            PendingInspectionSlotStatus.COMPLETED -> return next == PendingInspectionSlotStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: PendingInspectionSlotStatus) {
        if (!canMoveTo(next)) {
            error("pending inspection slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun netValue(): Double = round((windowDays.toDouble() - limit.toDouble()) * 100) / 100

    fun snapshot(): PendingInspectionSlot {
        return PendingInspectionSlot(
            code = code,
            name = name,
            routeCode = routeCode,
            limit = limit,
            windowDays = windowDays,
            category = category,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a pending inspection slot in status planned. */
fun newPendingInspectionSlot(code: String, name: String): PendingInspectionSlot {
    return PendingInspectionSlot(
        code = code,
        name = name,
        routeCode = "",
        limit = 0,
        windowDays = 0,
        category = "",
        shiftCode = "",
        status = PendingInspectionSlotStatus.PLANNED,
    )
}
