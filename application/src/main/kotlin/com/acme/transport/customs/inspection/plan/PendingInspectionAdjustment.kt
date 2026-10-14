package com.acme.transport.customs.inspection.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending inspection adjustment is in its life cycle. */
enum class PendingInspectionAdjustmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A pending inspection adjustment. Its code is unique within a tenant. */
data class PendingInspectionAdjustment(
    var code: String,
    var name: String,
    var durationMinutes: Int,
    var label: String,
    var depot: String,
    var threshold: Double,
    var category: String,
    var archived: Boolean,
    var active: Boolean,
    var amount: Double,
    var trailerId: String,
    var status: PendingInspectionAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingInspectionAdjustmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingInspectionAdjustmentStatus): Boolean {
        when (status) {
            PendingInspectionAdjustmentStatus.OPEN -> return next == PendingInspectionAdjustmentStatus.IN_REVIEW || next == PendingInspectionAdjustmentStatus.RESOLVED
            PendingInspectionAdjustmentStatus.IN_REVIEW -> {
                when (next) {
                    PendingInspectionAdjustmentStatus.REOPENED -> return true
                }
                return false
            }
            PendingInspectionAdjustmentStatus.RESOLVED -> return next == PendingInspectionAdjustmentStatus.REOPENED || next == PendingInspectionAdjustmentStatus.ARCHIVED
            PendingInspectionAdjustmentStatus.REOPENED -> {
                when (next) {
                    PendingInspectionAdjustmentStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingInspectionAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("pending inspection adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$name]"

    fun effectiveVolume(): Double = round((amount - threshold) * 100) / 100

    fun snapshot(): PendingInspectionAdjustment {
        return PendingInspectionAdjustment(
            code = code,
            name = name,
            durationMinutes = durationMinutes,
            label = label,
            depot = depot,
            threshold = threshold,
            category = category,
            archived = archived,
            active = active,
            amount = amount,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a pending inspection adjustment in status open. */
fun newPendingInspectionAdjustment(code: String, name: String): PendingInspectionAdjustment {
    return PendingInspectionAdjustment(
        code = code,
        name = name,
        durationMinutes = 0,
        label = "",
        depot = "",
        threshold = 0.0,
        category = "",
        archived = false,
        active = false,
        amount = 0.0,
        trailerId = "",
        status = PendingInspectionAdjustmentStatus.OPEN,
    )
}
