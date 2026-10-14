package com.acme.transport.fleetops.inspection.inspectionbatch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling inspection draft is in its life cycle. */
enum class RollingInspectionDraftStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One rolling inspection draft, with what the planners need to act on it. */
data class RollingInspectionDraft(
    var code: String,
    var title: String,
    var fuelLiters: Double,
    var description: String,
    var ownerId: String,
    var amount: Double,
    var reviewer: String,
    var stopCount: Int,
    var approved: Boolean,
    var etaMinutes: Int,
    var createdAt: Int,
    var status: RollingInspectionDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingInspectionDraftStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingInspectionDraftStatus): Boolean {
        when (status) {
            RollingInspectionDraftStatus.CREATED -> return next == RollingInspectionDraftStatus.PUBLISHED
            RollingInspectionDraftStatus.VALIDATED -> {
                when (next) {
                    RollingInspectionDraftStatus.PUBLISHED, RollingInspectionDraftStatus.RETIRED -> return true
                }
                return false
            }
            RollingInspectionDraftStatus.PUBLISHED -> {
                when (next) {
                    RollingInspectionDraftStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingInspectionDraftStatus) {
        if (!canMoveTo(next)) {
            error("rolling inspection draft $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"rolling inspection draft"} $code"

    fun weightedTotal(): Double = round((fuelLiters + amount) * 100) / 100

    fun snapshot(): RollingInspectionDraft {
        return RollingInspectionDraft(
            code = code,
            title = title,
            fuelLiters = fuelLiters,
            description = description,
            ownerId = ownerId,
            amount = amount,
            reviewer = reviewer,
            stopCount = stopCount,
            approved = approved,
            etaMinutes = etaMinutes,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling inspection draft in status created. */
fun newRollingInspectionDraft(code: String, title: String): RollingInspectionDraft {
    return RollingInspectionDraft(
        code = code,
        title = title,
        fuelLiters = 0.0,
        description = "",
        ownerId = "",
        amount = 0.0,
        reviewer = "",
        stopCount = 0,
        approved = false,
        etaMinutes = 0,
        createdAt = 0,
        status = RollingInspectionDraftStatus.CREATED,
    )
}
