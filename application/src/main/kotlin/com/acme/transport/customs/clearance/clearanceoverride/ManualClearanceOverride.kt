package com.acme.transport.customs.clearance.clearanceoverride

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual clearance override records of customs's clearance work, and the rules, stores, and calculations
// around them.

/** Where a manual clearance override is in its life cycle. */
enum class ManualClearanceOverrideStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A manual clearance override. Its code is unique within a tenant. */
data class ManualClearanceOverride(
    var code: String,
    var name: String,
    var endsAt: Int,
    var limit: Int,
    var cost: Double,
    var ceiling: Double,
    var source: String,
    var status: ManualClearanceOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualClearanceOverrideStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualClearanceOverrideStatus): Boolean {
        when (status) {
            ManualClearanceOverrideStatus.PLANNED -> {
                when (next) {
                    ManualClearanceOverrideStatus.SCHEDULED, ManualClearanceOverrideStatus.IN_PROGRESS -> return true
                }
                return false
            }
            ManualClearanceOverrideStatus.SCHEDULED -> return next == ManualClearanceOverrideStatus.CANCELLED
            ManualClearanceOverrideStatus.IN_PROGRESS -> return next == ManualClearanceOverrideStatus.CANCELLED
            ManualClearanceOverrideStatus.COMPLETED -> {
                when (next) {
                    ManualClearanceOverrideStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualClearanceOverrideStatus) {
        if (!canMoveTo(next)) {
            error("manual clearance override $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"manual clearance override"} $code"

    fun netLoad(): Double = round(limit.toDouble() * endsAt.toDouble() * 100) / 100

    fun snapshot(): ManualClearanceOverride {
        return ManualClearanceOverride(
            code = code,
            name = name,
            endsAt = endsAt,
            limit = limit,
            cost = cost,
            ceiling = ceiling,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a manual clearance override in status planned. */
fun newManualClearanceOverride(code: String, name: String): ManualClearanceOverride {
    return ManualClearanceOverride(
        code = code,
        name = name,
        endsAt = 0,
        limit = 0,
        cost = 0.0,
        ceiling = 0.0,
        source = "",
        status = ManualClearanceOverrideStatus.PLANNED,
    )
}
