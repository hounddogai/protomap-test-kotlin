package com.acme.transport.safety.incident.incidentclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a incident adjustment is in its life cycle. */
enum class IncidentAdjustmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A incident adjustment as the incident team keeps it. */
data class IncidentAdjustment(
    var code: String,
    var label: String,
    var visible: Boolean,
    var attempts: Int,
    var routeCode: String,
    var title: String,
    var createdAt: Int,
    var trailerId: String,
    var category: String,
    var reviewer: String,
    var summary: String,
    var status: IncidentAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentAdjustmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentAdjustmentStatus): Boolean {
        when (status) {
            IncidentAdjustmentStatus.PROPOSED -> {
                when (next) {
                    IncidentAdjustmentStatus.APPROVED -> return true
                }
                return false
            }
            IncidentAdjustmentStatus.APPROVED -> return next == IncidentAdjustmentStatus.REJECTED || next == IncidentAdjustmentStatus.WITHDRAWN
            IncidentAdjustmentStatus.REJECTED -> {
                when (next) {
                    IncidentAdjustmentStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IncidentAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("incident adjustment $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveVolume(): Double = round(createdAt.toDouble() * attempts.toDouble() * 100) / 100

    fun snapshot(): IncidentAdjustment {
        return IncidentAdjustment(
            code = code,
            label = label,
            visible = visible,
            attempts = attempts,
            routeCode = routeCode,
            title = title,
            createdAt = createdAt,
            trailerId = trailerId,
            category = category,
            reviewer = reviewer,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a incident adjustment in status proposed. */
fun newIncidentAdjustment(code: String, label: String): IncidentAdjustment {
    return IncidentAdjustment(
        code = code,
        label = label,
        visible = false,
        attempts = 0,
        routeCode = "",
        title = "",
        createdAt = 0,
        trailerId = "",
        category = "",
        reviewer = "",
        summary = "",
        status = IncidentAdjustmentStatus.PROPOSED,
    )
}
