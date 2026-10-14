package com.acme.transport.safety.incident.incidentplan

import com.acme.transport.kit.text.normalize

// The regional incident plan records of safety's incident work, and the rules, stores, and calculations
// around them.

/** Where a regional incident plan is in its life cycle. */
enum class RegionalIncidentPlanStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A regional incident plan. Its code is unique within a tenant. */
data class RegionalIncidentPlan(
    var code: String,
    var title: String,
    var externalRef: String,
    var category: String,
    var dock: String,
    var reviewer: String,
    var description: String,
    var score: Double,
    var status: RegionalIncidentPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalIncidentPlanStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalIncidentPlanStatus): Boolean {
        when (status) {
            RegionalIncidentPlanStatus.OPEN -> return next == RegionalIncidentPlanStatus.IN_REVIEW
            RegionalIncidentPlanStatus.IN_REVIEW -> return next == RegionalIncidentPlanStatus.REOPENED || next == RegionalIncidentPlanStatus.ARCHIVED
            RegionalIncidentPlanStatus.RESOLVED -> return next == RegionalIncidentPlanStatus.REOPENED || next == RegionalIncidentPlanStatus.ARCHIVED
            RegionalIncidentPlanStatus.REOPENED -> return next == RegionalIncidentPlanStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RegionalIncidentPlanStatus) {
        if (!canMoveTo(next)) {
            error("regional incident plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun measuredValue(): Double = score

    fun snapshot(): RegionalIncidentPlan {
        return RegionalIncidentPlan(
            code = code,
            title = title,
            externalRef = externalRef,
            category = category,
            dock = dock,
            reviewer = reviewer,
            description = description,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a regional incident plan in status open. */
fun newRegionalIncidentPlan(code: String, title: String): RegionalIncidentPlan {
    return RegionalIncidentPlan(
        code = code,
        title = title,
        externalRef = "",
        category = "",
        dock = "",
        reviewer = "",
        description = "",
        score = 0.0,
        status = RegionalIncidentPlanStatus.OPEN,
    )
}
