package com.acme.transport.fleetops.inspection.rollup

import com.acme.transport.kit.text.normalize

// The inspection rollup records of fleetops's inspection work, and the rules, stores, and calculations around
// them.

/** Where a inspection rollup is in its life cycle. */
enum class InspectionRollupStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a inspection rollup and its current status. */
data class InspectionRollup(
    var code: String,
    var label: String,
    var contactEmail: String,
    var pinned: Boolean,
    var country: String,
    var score: Double,
    var status: InspectionRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionRollupStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionRollupStatus): Boolean {
        when (status) {
            InspectionRollupStatus.OPEN -> return next == InspectionRollupStatus.REOPENED || next == InspectionRollupStatus.ARCHIVED
            InspectionRollupStatus.IN_REVIEW -> {
                when (next) {
                    InspectionRollupStatus.RESOLVED, InspectionRollupStatus.REOPENED -> return true
                }
                return false
            }
            InspectionRollupStatus.RESOLVED -> return next == InspectionRollupStatus.REOPENED || next == InspectionRollupStatus.ARCHIVED
            InspectionRollupStatus.REOPENED -> return next == InspectionRollupStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: InspectionRollupStatus) {
        if (!canMoveTo(next)) {
            error("inspection rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = score

    fun snapshot(): InspectionRollup {
        return InspectionRollup(
            code = code,
            label = label,
            contactEmail = contactEmail,
            pinned = pinned,
            country = country,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a inspection rollup in status open. */
fun newInspectionRollup(code: String, label: String): InspectionRollup {
    return InspectionRollup(
        code = code,
        label = label,
        contactEmail = "",
        pinned = false,
        country = "",
        score = 0.0,
        status = InspectionRollupStatus.OPEN,
    )
}
