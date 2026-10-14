package com.acme.transport.customs.inspection.overrideitem

import com.acme.transport.kit.text.normalize

// The inspection override records of customs's inspection work, and the rules, stores, and calculations
// around them.

/** Where a inspection override is in its life cycle. */
enum class InspectionOverrideStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A inspection override as the inspection team keeps it. */
data class InspectionOverride(
    var code: String,
    var name: String,
    var pinned: Boolean,
    var active: Boolean,
    var attempts: Int,
    var status: InspectionOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionOverrideStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionOverrideStatus): Boolean {
        when (status) {
            InspectionOverrideStatus.DRAFT -> {
                when (next) {
                    InspectionOverrideStatus.ACTIVE -> return true
                }
                return false
            }
            InspectionOverrideStatus.PENDING -> {
                when (next) {
                    InspectionOverrideStatus.SUSPENDED -> return true
                }
                return false
            }
            InspectionOverrideStatus.ACTIVE -> return next == InspectionOverrideStatus.SUSPENDED
            InspectionOverrideStatus.SUSPENDED -> return next == InspectionOverrideStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: InspectionOverrideStatus) {
        if (!canMoveTo(next)) {
            error("inspection override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"inspection override"} $code"

    fun measuredValue(): Double = attempts.toDouble()

    fun snapshot(): InspectionOverride {
        return InspectionOverride(
            code = code,
            name = name,
            pinned = pinned,
            active = active,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a inspection override in status draft. */
fun newInspectionOverride(code: String, name: String): InspectionOverride {
    return InspectionOverride(
        code = code,
        name = name,
        pinned = false,
        active = false,
        attempts = 0,
        status = InspectionOverrideStatus.DRAFT,
    )
}
