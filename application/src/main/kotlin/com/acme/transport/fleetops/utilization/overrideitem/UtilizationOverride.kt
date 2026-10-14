package com.acme.transport.fleetops.utilization.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The utilization override records of fleetops's utilization work, and the rules, stores, and calculations
// around them.

/** Where a utilization override is in its life cycle. */
enum class UtilizationOverrideStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a utilization override and its current status. */
data class UtilizationOverride(
    var code: String,
    var title: String,
    var category: String,
    var attributes: MutableMap<String, String>,
    var segment: String,
    var stopCount: Int,
    var windowDays: Int,
    var currency: String,
    var status: UtilizationOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationOverrideStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationOverrideStatus): Boolean {
        when (status) {
            UtilizationOverrideStatus.PLANNED -> {
                when (next) {
                    UtilizationOverrideStatus.IN_PROGRESS, UtilizationOverrideStatus.CANCELLED -> return true
                }
                return false
            }
            UtilizationOverrideStatus.SCHEDULED -> return next == UtilizationOverrideStatus.IN_PROGRESS
            UtilizationOverrideStatus.IN_PROGRESS -> return next == UtilizationOverrideStatus.CANCELLED
            UtilizationOverrideStatus.COMPLETED -> return next == UtilizationOverrideStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: UtilizationOverrideStatus) {
        if (!canMoveTo(next)) {
            error("utilization override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netVolume(): Double = round((stopCount.toDouble() - windowDays.toDouble()) * 100) / 100

    fun snapshot(): UtilizationOverride {
        return UtilizationOverride(
            code = code,
            title = title,
            category = category,
            attributes = attributes,
            segment = segment,
            stopCount = stopCount,
            windowDays = windowDays,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a utilization override in status planned. */
fun newUtilizationOverride(code: String, title: String): UtilizationOverride {
    return UtilizationOverride(
        code = code,
        title = title,
        category = "",
        attributes = mutableMapOf<String, String>(),
        segment = "",
        stopCount = 0,
        windowDays = 0,
        currency = "",
        status = UtilizationOverrideStatus.PLANNED,
    )
}
