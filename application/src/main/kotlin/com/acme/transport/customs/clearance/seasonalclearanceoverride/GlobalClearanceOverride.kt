package com.acme.transport.customs.clearance.seasonalclearanceoverride

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global clearance override records of customs's clearance work, and the rules, stores, and calculations
// around them.

/** Where a global clearance override is in its life cycle. */
enum class GlobalClearanceOverrideStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One global clearance override, with what the planners need to act on it. */
data class GlobalClearanceOverride(
    var code: String,
    var title: String,
    var driverId: String,
    var stopCount: Int,
    var loadKg: Double,
    var attempts: Int,
    var rate: Double,
    var country: String,
    var maximum: Double,
    var timezone: String,
    var attributes: MutableMap<String, String>,
    var status: GlobalClearanceOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalClearanceOverrideStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalClearanceOverrideStatus): Boolean {
        when (status) {
            GlobalClearanceOverrideStatus.DRAFT -> return next == GlobalClearanceOverrideStatus.ACTIVE
            GlobalClearanceOverrideStatus.PENDING -> return next == GlobalClearanceOverrideStatus.SUSPENDED
            GlobalClearanceOverrideStatus.ACTIVE -> return next == GlobalClearanceOverrideStatus.CLOSED
            GlobalClearanceOverrideStatus.SUSPENDED -> return next == GlobalClearanceOverrideStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: GlobalClearanceOverrideStatus) {
        if (!canMoveTo(next)) {
            error("global clearance override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedLoad(): Double = round((rate + stopCount.toDouble()) * 100) / 100

    fun snapshot(): GlobalClearanceOverride {
        return GlobalClearanceOverride(
            code = code,
            title = title,
            driverId = driverId,
            stopCount = stopCount,
            loadKg = loadKg,
            attempts = attempts,
            rate = rate,
            country = country,
            maximum = maximum,
            timezone = timezone,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a global clearance override in status draft. */
fun newGlobalClearanceOverride(code: String, title: String): GlobalClearanceOverride {
    return GlobalClearanceOverride(
        code = code,
        title = title,
        driverId = "",
        stopCount = 0,
        loadKg = 0.0,
        attempts = 0,
        rate = 0.0,
        country = "",
        maximum = 0.0,
        timezone = "",
        attributes = mutableMapOf<String, String>(),
        status = GlobalClearanceOverrideStatus.DRAFT,
    )
}
