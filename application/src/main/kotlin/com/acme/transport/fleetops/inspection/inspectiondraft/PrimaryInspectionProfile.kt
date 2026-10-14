package com.acme.transport.fleetops.inspection.inspectiondraft

import com.acme.transport.kit.text.normalize

/** Where a primary inspection profile is in its life cycle. */
enum class PrimaryInspectionProfileStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One primary inspection profile, with what the planners need to act on it. */
data class PrimaryInspectionProfile(
    var code: String,
    var title: String,
    var active: Boolean,
    var source: String,
    var summary: String,
    var score: Double,
    var status: PrimaryInspectionProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryInspectionProfileStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryInspectionProfileStatus): Boolean {
        when (status) {
            PrimaryInspectionProfileStatus.OPEN -> {
                when (next) {
                    PrimaryInspectionProfileStatus.REOPENED -> return true
                }
                return false
            }
            PrimaryInspectionProfileStatus.IN_REVIEW -> {
                when (next) {
                    PrimaryInspectionProfileStatus.RESOLVED, PrimaryInspectionProfileStatus.REOPENED -> return true
                }
                return false
            }
            PrimaryInspectionProfileStatus.RESOLVED -> {
                when (next) {
                    PrimaryInspectionProfileStatus.REOPENED, PrimaryInspectionProfileStatus.ARCHIVED -> return true
                }
                return false
            }
            PrimaryInspectionProfileStatus.REOPENED -> return next == PrimaryInspectionProfileStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: PrimaryInspectionProfileStatus) {
        if (!canMoveTo(next)) {
            error("primary inspection profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"primary inspection profile"} $code"

    fun measuredValue(): Double = score

    fun snapshot(): PrimaryInspectionProfile {
        return PrimaryInspectionProfile(
            code = code,
            title = title,
            active = active,
            source = source,
            summary = summary,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a primary inspection profile in status open. */
fun newPrimaryInspectionProfile(code: String, title: String): PrimaryInspectionProfile {
    return PrimaryInspectionProfile(
        code = code,
        title = title,
        active = false,
        source = "",
        summary = "",
        score = 0.0,
        status = PrimaryInspectionProfileStatus.OPEN,
    )
}
