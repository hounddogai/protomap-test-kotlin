package com.acme.transport.fleetops.inspection.inspectiondraft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy inspection draft records of fleetops's inspection work, and the rules, stores, and calculations
// around them.

/** Where a legacy inspection draft is in its life cycle. */
enum class LegacyInspectionDraftStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A legacy inspection draft. Its code is unique within a tenant. */
data class LegacyInspectionDraft(
    var code: String,
    var label: String,
    var version: Int,
    var odometer: Int,
    var country: String,
    var sequence: Int,
    var trailerId: String,
    var status: LegacyInspectionDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyInspectionDraftStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyInspectionDraftStatus): Boolean {
        when (status) {
            LegacyInspectionDraftStatus.OPEN -> {
                when (next) {
                    LegacyInspectionDraftStatus.ARCHIVED -> return true
                }
                return false
            }
            LegacyInspectionDraftStatus.IN_REVIEW -> {
                when (next) {
                    LegacyInspectionDraftStatus.RESOLVED -> return true
                }
                return false
            }
            LegacyInspectionDraftStatus.RESOLVED -> return next == LegacyInspectionDraftStatus.REOPENED || next == LegacyInspectionDraftStatus.ARCHIVED
            LegacyInspectionDraftStatus.REOPENED -> return next == LegacyInspectionDraftStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: LegacyInspectionDraftStatus) {
        if (!canMoveTo(next)) {
            error("legacy inspection draft $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveExposure(): Double = round((sequence.toDouble() - version.toDouble()) * 100) / 100

    fun snapshot(): LegacyInspectionDraft {
        return LegacyInspectionDraft(
            code = code,
            label = label,
            version = version,
            odometer = odometer,
            country = country,
            sequence = sequence,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a legacy inspection draft in status open. */
fun newLegacyInspectionDraft(code: String, label: String): LegacyInspectionDraft {
    return LegacyInspectionDraft(
        code = code,
        label = label,
        version = 0,
        odometer = 0,
        country = "",
        sequence = 0,
        trailerId = "",
        status = LegacyInspectionDraftStatus.OPEN,
    )
}
