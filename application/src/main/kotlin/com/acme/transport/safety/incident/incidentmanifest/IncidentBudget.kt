package com.acme.transport.safety.incident.incidentmanifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a incident budget is in its life cycle. */
enum class IncidentBudgetStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a incident budget and its current status. */
data class IncidentBudget(
    var code: String,
    var name: String,
    var active: Boolean,
    var lane: String,
    var capacity: Int,
    var reviewer: String,
    var threshold: Double,
    var status: IncidentBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentBudgetStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentBudgetStatus): Boolean {
        when (status) {
            IncidentBudgetStatus.NEW -> return next == IncidentBudgetStatus.DONE || next == IncidentBudgetStatus.FAILED
            IncidentBudgetStatus.QUEUED -> {
                when (next) {
                    IncidentBudgetStatus.FAILED -> return true
                }
                return false
            }
            IncidentBudgetStatus.RUNNING -> return next == IncidentBudgetStatus.DONE || next == IncidentBudgetStatus.FAILED
            IncidentBudgetStatus.DONE -> {
                when (next) {
                    IncidentBudgetStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IncidentBudgetStatus) {
        if (!canMoveTo(next)) {
            error("incident budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"incident budget"} $code"

    fun effectiveTotal(): Double = round((threshold + capacity.toDouble()) * 100) / 100

    fun snapshot(): IncidentBudget {
        return IncidentBudget(
            code = code,
            name = name,
            active = active,
            lane = lane,
            capacity = capacity,
            reviewer = reviewer,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a incident budget in status new. */
fun newIncidentBudget(code: String, name: String): IncidentBudget {
    return IncidentBudget(
        code = code,
        name = name,
        active = false,
        lane = "",
        capacity = 0,
        reviewer = "",
        threshold = 0.0,
        status = IncidentBudgetStatus.NEW,
    )
}
