package com.acme.transport.customs.inspection.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary inspection slot is in its life cycle. */
enum class PrimaryInspectionSlotStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One primary inspection slot, with what the planners need to act on it. */
data class PrimaryInspectionSlot(
    var code: String,
    var label: String,
    var shiftCode: String,
    var ceiling: Double,
    var floorValue: Double,
    var summary: String,
    var graceDays: Int,
    var retries: Int,
    var category: String,
    var revision: Int,
    var status: PrimaryInspectionSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryInspectionSlotStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryInspectionSlotStatus): Boolean {
        when (status) {
            PrimaryInspectionSlotStatus.OPEN -> {
                when (next) {
                    PrimaryInspectionSlotStatus.RESOLVED, PrimaryInspectionSlotStatus.REOPENED -> return true
                }
                return false
            }
            PrimaryInspectionSlotStatus.IN_REVIEW -> return next == PrimaryInspectionSlotStatus.REOPENED
            PrimaryInspectionSlotStatus.RESOLVED -> return next == PrimaryInspectionSlotStatus.REOPENED || next == PrimaryInspectionSlotStatus.ARCHIVED
            PrimaryInspectionSlotStatus.REOPENED -> {
                when (next) {
                    PrimaryInspectionSlotStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryInspectionSlotStatus) {
        if (!canMoveTo(next)) {
            error("primary inspection slot $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedExposure(): Double = round((floorValue - ceiling) * 100) / 100

    fun snapshot(): PrimaryInspectionSlot {
        return PrimaryInspectionSlot(
            code = code,
            label = label,
            shiftCode = shiftCode,
            ceiling = ceiling,
            floorValue = floorValue,
            summary = summary,
            graceDays = graceDays,
            retries = retries,
            category = category,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a primary inspection slot in status open. */
fun newPrimaryInspectionSlot(code: String, label: String): PrimaryInspectionSlot {
    return PrimaryInspectionSlot(
        code = code,
        label = label,
        shiftCode = "",
        ceiling = 0.0,
        floorValue = 0.0,
        summary = "",
        graceDays = 0,
        retries = 0,
        category = "",
        revision = 0,
        status = PrimaryInspectionSlotStatus.OPEN,
    )
}
