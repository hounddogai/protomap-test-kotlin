package com.acme.transport.safety.incident.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a incident target is in its life cycle. */
enum class IncidentTargetStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A incident target as the incident team keeps it. */
data class IncidentTarget(
    var code: String,
    var name: String,
    var etaMinutes: Int,
    var budget: Double,
    var cost: Double,
    var weight: Double,
    var threshold: Double,
    var discount: Double,
    var margin: Double,
    var status: IncidentTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentTargetStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentTargetStatus): Boolean {
        when (status) {
            IncidentTargetStatus.PLANNED -> return next == IncidentTargetStatus.SCHEDULED || next == IncidentTargetStatus.COMPLETED
            IncidentTargetStatus.SCHEDULED -> {
                when (next) {
                    IncidentTargetStatus.COMPLETED, IncidentTargetStatus.CANCELLED -> return true
                }
                return false
            }
            IncidentTargetStatus.IN_PROGRESS -> {
                when (next) {
                    IncidentTargetStatus.CANCELLED -> return true
                }
                return false
            }
            IncidentTargetStatus.COMPLETED -> {
                when (next) {
                    IncidentTargetStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IncidentTargetStatus) {
        if (!canMoveTo(next)) {
            error("incident target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"incident target"} $code"

    fun weightedTotal(): Double = round((discount - weight) * 100) / 100

    fun snapshot(): IncidentTarget {
        return IncidentTarget(
            code = code,
            name = name,
            etaMinutes = etaMinutes,
            budget = budget,
            cost = cost,
            weight = weight,
            threshold = threshold,
            discount = discount,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a incident target in status planned. */
fun newIncidentTarget(code: String, name: String): IncidentTarget {
    return IncidentTarget(
        code = code,
        name = name,
        etaMinutes = 0,
        budget = 0.0,
        cost = 0.0,
        weight = 0.0,
        threshold = 0.0,
        discount = 0.0,
        margin = 0.0,
        status = IncidentTargetStatus.PLANNED,
    )
}
