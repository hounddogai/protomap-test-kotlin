package com.acme.transport.customs.clearance.primaryclearanceoverride

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional clearance override records of customs's clearance work, and the rules, stores, and
// calculations around them.

/** Where a regional clearance override is in its life cycle. */
enum class RegionalClearanceOverrideStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a regional clearance override and its current status. */
data class RegionalClearanceOverride(
    var code: String,
    var label: String,
    var ceiling: Double,
    var notes: String,
    var category: String,
    var weight: Double,
    var status: RegionalClearanceOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalClearanceOverrideStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalClearanceOverrideStatus): Boolean {
        when (status) {
            RegionalClearanceOverrideStatus.IDLE -> {
                when (next) {
                    RegionalClearanceOverrideStatus.WARMING, RegionalClearanceOverrideStatus.DRAINING -> return true
                }
                return false
            }
            RegionalClearanceOverrideStatus.WARMING -> {
                when (next) {
                    RegionalClearanceOverrideStatus.DRAINING, RegionalClearanceOverrideStatus.STOPPED -> return true
                }
                return false
            }
            RegionalClearanceOverrideStatus.READY -> return next == RegionalClearanceOverrideStatus.DRAINING || next == RegionalClearanceOverrideStatus.STOPPED
            RegionalClearanceOverrideStatus.DRAINING -> {
                when (next) {
                    RegionalClearanceOverrideStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalClearanceOverrideStatus) {
        if (!canMoveTo(next)) {
            error("regional clearance override $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveLoad(): Double = round((weight + ceiling) * 100) / 100

    fun snapshot(): RegionalClearanceOverride {
        return RegionalClearanceOverride(
            code = code,
            label = label,
            ceiling = ceiling,
            notes = notes,
            category = category,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a regional clearance override in status idle. */
fun newRegionalClearanceOverride(code: String, label: String): RegionalClearanceOverride {
    return RegionalClearanceOverride(
        code = code,
        label = label,
        ceiling = 0.0,
        notes = "",
        category = "",
        weight = 0.0,
        status = RegionalClearanceOverrideStatus.IDLE,
    )
}
