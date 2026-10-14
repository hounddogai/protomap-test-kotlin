package com.acme.transport.safety.scorecard.scorecardbudget

import com.acme.transport.kit.text.normalize

/** Where a seasonal scorecard assignment is in its life cycle. */
enum class SeasonalScorecardAssignmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A seasonal scorecard assignment. Its code is unique within a tenant. */
data class SeasonalScorecardAssignment(
    var code: String,
    var label: String,
    var name: String,
    var notes: String,
    var country: String,
    var routeCode: String,
    var distanceKm: Double,
    var status: SeasonalScorecardAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalScorecardAssignmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalScorecardAssignmentStatus): Boolean {
        when (status) {
            SeasonalScorecardAssignmentStatus.OPEN -> return next == SeasonalScorecardAssignmentStatus.RESOLVED
            SeasonalScorecardAssignmentStatus.IN_REVIEW -> return next == SeasonalScorecardAssignmentStatus.ARCHIVED
            SeasonalScorecardAssignmentStatus.RESOLVED -> return next == SeasonalScorecardAssignmentStatus.REOPENED || next == SeasonalScorecardAssignmentStatus.ARCHIVED
            SeasonalScorecardAssignmentStatus.REOPENED -> return next == SeasonalScorecardAssignmentStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: SeasonalScorecardAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("seasonal scorecard assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = distanceKm

    fun snapshot(): SeasonalScorecardAssignment {
        return SeasonalScorecardAssignment(
            code = code,
            label = label,
            name = name,
            notes = notes,
            country = country,
            routeCode = routeCode,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a seasonal scorecard assignment in status open. */
fun newSeasonalScorecardAssignment(code: String, label: String): SeasonalScorecardAssignment {
    return SeasonalScorecardAssignment(
        code = code,
        label = label,
        name = "",
        notes = "",
        country = "",
        routeCode = "",
        distanceKm = 0.0,
        status = SeasonalScorecardAssignmentStatus.OPEN,
    )
}
