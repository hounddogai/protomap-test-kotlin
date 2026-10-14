package com.acme.transport.emissions.routeoffset.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual route offset baseline records of emissions's route offset work, and the rules, stores, and
// calculations around them.

/** Where a manual route offset baseline is in its life cycle. */
enum class ManualRouteOffsetBaselineStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A manual route offset baseline as the route offset team keeps it. */
data class ManualRouteOffsetBaseline(
    var code: String,
    var name: String,
    var label: String,
    var vehicleId: String,
    var minimum: Double,
    var loadKg: Double,
    var weight: Double,
    var unit: String,
    var attributes: MutableMap<String, String>,
    var title: String,
    var score: Double,
    var status: ManualRouteOffsetBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualRouteOffsetBaselineStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualRouteOffsetBaselineStatus): Boolean {
        when (status) {
            ManualRouteOffsetBaselineStatus.CREATED -> return next == ManualRouteOffsetBaselineStatus.VALIDATED || next == ManualRouteOffsetBaselineStatus.RETIRED
            ManualRouteOffsetBaselineStatus.VALIDATED -> {
                when (next) {
                    ManualRouteOffsetBaselineStatus.RETIRED -> return true
                }
                return false
            }
            ManualRouteOffsetBaselineStatus.PUBLISHED -> return next == ManualRouteOffsetBaselineStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ManualRouteOffsetBaselineStatus) {
        if (!canMoveTo(next)) {
            error("manual route offset baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$name]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedLoad(): Double = round(score * loadKg * 100) / 100

    fun snapshot(): ManualRouteOffsetBaseline {
        return ManualRouteOffsetBaseline(
            code = code,
            name = name,
            label = label,
            vehicleId = vehicleId,
            minimum = minimum,
            loadKg = loadKg,
            weight = weight,
            unit = unit,
            attributes = attributes,
            title = title,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a manual route offset baseline in status created. */
fun newManualRouteOffsetBaseline(code: String, name: String): ManualRouteOffsetBaseline {
    return ManualRouteOffsetBaseline(
        code = code,
        name = name,
        label = "",
        vehicleId = "",
        minimum = 0.0,
        loadKg = 0.0,
        weight = 0.0,
        unit = "",
        attributes = mutableMapOf<String, String>(),
        title = "",
        score = 0.0,
        status = ManualRouteOffsetBaselineStatus.CREATED,
    )
}
