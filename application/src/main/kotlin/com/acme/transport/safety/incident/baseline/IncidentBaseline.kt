package com.acme.transport.safety.incident.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The incident baseline records of safety's incident work, and the rules, stores, and calculations around
// them.

/** Where a incident baseline is in its life cycle. */
enum class IncidentBaselineStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A incident baseline. Its code is unique within a tenant. */
data class IncidentBaseline(
    var code: String,
    var label: String,
    var tags: MutableList<String>,
    var fuelLiters: Double,
    var odometer: Int,
    var category: String,
    var priority: Int,
    var status: IncidentBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentBaselineStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentBaselineStatus): Boolean {
        when (status) {
            IncidentBaselineStatus.PROPOSED -> return next == IncidentBaselineStatus.WITHDRAWN
            IncidentBaselineStatus.APPROVED -> return next == IncidentBaselineStatus.REJECTED || next == IncidentBaselineStatus.WITHDRAWN
            IncidentBaselineStatus.REJECTED -> {
                when (next) {
                    IncidentBaselineStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IncidentBaselineStatus) {
        if (!canMoveTo(next)) {
            error("incident baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedTotal(): Double = round((priority.toDouble() - fuelLiters) * 100) / 100

    fun snapshot(): IncidentBaseline {
        return IncidentBaseline(
            code = code,
            label = label,
            tags = tags,
            fuelLiters = fuelLiters,
            odometer = odometer,
            category = category,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a incident baseline in status proposed. */
fun newIncidentBaseline(code: String, label: String): IncidentBaseline {
    return IncidentBaseline(
        code = code,
        label = label,
        tags = mutableListOf<String>(),
        fuelLiters = 0.0,
        odometer = 0,
        category = "",
        priority = 0,
        status = IncidentBaselineStatus.PROPOSED,
    )
}
