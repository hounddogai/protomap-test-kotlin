package com.acme.transport.fleetops.inspection.inspectionbatch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped inspection draft is in its life cycle. */
enum class CappedInspectionDraftStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A capped inspection draft as the inspection team keeps it. */
data class CappedInspectionDraft(
    var code: String,
    var name: String,
    var revision: Int,
    var cost: Double,
    var pallets: Int,
    var etaMinutes: Int,
    var status: CappedInspectionDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedInspectionDraftStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedInspectionDraftStatus): Boolean {
        when (status) {
            CappedInspectionDraftStatus.PLANNED -> {
                when (next) {
                    CappedInspectionDraftStatus.CANCELLED -> return true
                }
                return false
            }
            CappedInspectionDraftStatus.SCHEDULED -> {
                when (next) {
                    CappedInspectionDraftStatus.IN_PROGRESS -> return true
                }
                return false
            }
            CappedInspectionDraftStatus.IN_PROGRESS -> {
                when (next) {
                    CappedInspectionDraftStatus.COMPLETED, CappedInspectionDraftStatus.CANCELLED -> return true
                }
                return false
            }
            CappedInspectionDraftStatus.COMPLETED -> return next == CappedInspectionDraftStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: CappedInspectionDraftStatus) {
        if (!canMoveTo(next)) {
            error("capped inspection draft $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code ($name)"

    fun projectedValue(): Double = round((cost + pallets.toDouble()) * 100) / 100

    fun snapshot(): CappedInspectionDraft {
        return CappedInspectionDraft(
            code = code,
            name = name,
            revision = revision,
            cost = cost,
            pallets = pallets,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a capped inspection draft in status planned. */
fun newCappedInspectionDraft(code: String, name: String): CappedInspectionDraft {
    return CappedInspectionDraft(
        code = code,
        name = name,
        revision = 0,
        cost = 0.0,
        pallets = 0,
        etaMinutes = 0,
        status = CappedInspectionDraftStatus.PLANNED,
    )
}
