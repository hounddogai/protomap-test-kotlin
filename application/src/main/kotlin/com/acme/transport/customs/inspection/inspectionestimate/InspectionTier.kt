package com.acme.transport.customs.inspection.inspectionestimate

import com.acme.transport.kit.text.normalize

/** Where a inspection tier is in its life cycle. */
enum class InspectionTierStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A inspection tier as the inspection team keeps it. */
data class InspectionTier(
    var code: String,
    var label: String,
    var routeCode: String,
    var durationMinutes: Int,
    var archived: Boolean,
    var reviewer: String,
    var notes: String,
    var status: InspectionTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionTierStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionTierStatus): Boolean {
        when (status) {
            InspectionTierStatus.OPEN -> {
                when (next) {
                    InspectionTierStatus.REOPENED, InspectionTierStatus.ARCHIVED -> return true
                }
                return false
            }
            InspectionTierStatus.IN_REVIEW -> return next == InspectionTierStatus.RESOLVED || next == InspectionTierStatus.REOPENED
            InspectionTierStatus.RESOLVED -> return next == InspectionTierStatus.REOPENED || next == InspectionTierStatus.ARCHIVED
            InspectionTierStatus.REOPENED -> return next == InspectionTierStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: InspectionTierStatus) {
        if (!canMoveTo(next)) {
            error("inspection tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = durationMinutes.toDouble()

    fun snapshot(): InspectionTier {
        return InspectionTier(
            code = code,
            label = label,
            routeCode = routeCode,
            durationMinutes = durationMinutes,
            archived = archived,
            reviewer = reviewer,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a inspection tier in status open. */
fun newInspectionTier(code: String, label: String): InspectionTier {
    return InspectionTier(
        code = code,
        label = label,
        routeCode = "",
        durationMinutes = 0,
        archived = false,
        reviewer = "",
        notes = "",
        status = InspectionTierStatus.OPEN,
    )
}
