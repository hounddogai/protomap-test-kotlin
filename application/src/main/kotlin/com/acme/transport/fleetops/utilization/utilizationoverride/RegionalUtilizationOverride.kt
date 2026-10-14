package com.acme.transport.fleetops.utilization.utilizationoverride

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional utilization override records of fleetops's utilization work, and the rules, stores, and
// calculations around them.

/** Where a regional utilization override is in its life cycle. */
enum class RegionalUtilizationOverrideStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a regional utilization override and its current status. */
data class RegionalUtilizationOverride(
    var code: String,
    var name: String,
    var source: String,
    var retries: Int,
    var priority: Int,
    var pinned: Boolean,
    var budget: Double,
    var durationMinutes: Int,
    var title: String,
    var status: RegionalUtilizationOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalUtilizationOverrideStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalUtilizationOverrideStatus): Boolean {
        when (status) {
            RegionalUtilizationOverrideStatus.CREATED -> return next == RegionalUtilizationOverrideStatus.VALIDATED
            RegionalUtilizationOverrideStatus.VALIDATED -> return next == RegionalUtilizationOverrideStatus.RETIRED
            RegionalUtilizationOverrideStatus.PUBLISHED -> {
                when (next) {
                    RegionalUtilizationOverrideStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalUtilizationOverrideStatus) {
        if (!canMoveTo(next)) {
            error("regional utilization override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"regional utilization override"} $code"

    fun adjustedValue(): Double = round((budget + priority.toDouble()) * 100) / 100

    fun snapshot(): RegionalUtilizationOverride {
        return RegionalUtilizationOverride(
            code = code,
            name = name,
            source = source,
            retries = retries,
            priority = priority,
            pinned = pinned,
            budget = budget,
            durationMinutes = durationMinutes,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a regional utilization override in status created. */
fun newRegionalUtilizationOverride(code: String, name: String): RegionalUtilizationOverride {
    return RegionalUtilizationOverride(
        code = code,
        name = name,
        source = "",
        retries = 0,
        priority = 0,
        pinned = false,
        budget = 0.0,
        durationMinutes = 0,
        title = "",
        status = RegionalUtilizationOverrideStatus.CREATED,
    )
}
