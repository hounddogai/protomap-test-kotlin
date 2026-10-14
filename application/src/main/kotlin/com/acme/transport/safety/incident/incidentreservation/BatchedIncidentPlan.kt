package com.acme.transport.safety.incident.incidentreservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched incident plan is in its life cycle. */
enum class BatchedIncidentPlanStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One batched incident plan, with what the planners need to act on it. */
data class BatchedIncidentPlan(
    var code: String,
    var label: String,
    var archived: Boolean,
    var routeCode: String,
    var capacity: Int,
    var depot: String,
    var attempts: Int,
    var externalRef: String,
    var windowDays: Int,
    var visible: Boolean,
    var budget: Double,
    var status: BatchedIncidentPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedIncidentPlanStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedIncidentPlanStatus): Boolean {
        when (status) {
            BatchedIncidentPlanStatus.REQUESTED -> return next == BatchedIncidentPlanStatus.REVOKED || next == BatchedIncidentPlanStatus.EXPIRED
            BatchedIncidentPlanStatus.GRANTED -> return next == BatchedIncidentPlanStatus.REVOKED
            BatchedIncidentPlanStatus.REVOKED -> return next == BatchedIncidentPlanStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: BatchedIncidentPlanStatus) {
        if (!canMoveTo(next)) {
            error("batched incident plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"batched incident plan"} $code"

    fun weightedTotal(): Double = round((budget - attempts.toDouble()) * 100) / 100

    fun snapshot(): BatchedIncidentPlan {
        return BatchedIncidentPlan(
            code = code,
            label = label,
            archived = archived,
            routeCode = routeCode,
            capacity = capacity,
            depot = depot,
            attempts = attempts,
            externalRef = externalRef,
            windowDays = windowDays,
            visible = visible,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a batched incident plan in status requested. */
fun newBatchedIncidentPlan(code: String, label: String): BatchedIncidentPlan {
    return BatchedIncidentPlan(
        code = code,
        label = label,
        archived = false,
        routeCode = "",
        capacity = 0,
        depot = "",
        attempts = 0,
        externalRef = "",
        windowDays = 0,
        visible = false,
        budget = 0.0,
        status = BatchedIncidentPlanStatus.REQUESTED,
    )
}
