package com.acme.transport.emissions.routeoffset.offsetmetric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive route offset milestone is in its life cycle. */
enum class AdaptiveRouteOffsetMilestoneStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a adaptive route offset milestone and its current status. */
data class AdaptiveRouteOffsetMilestone(
    var code: String,
    var title: String,
    var depot: String,
    var ownerId: String,
    var budget: Double,
    var loadKg: Double,
    var attributes: MutableMap<String, String>,
    var unit: String,
    var checksum: String,
    var maximum: Double,
    var status: AdaptiveRouteOffsetMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveRouteOffsetMilestoneStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveRouteOffsetMilestoneStatus): Boolean {
        when (status) {
            AdaptiveRouteOffsetMilestoneStatus.NEW -> {
                when (next) {
                    AdaptiveRouteOffsetMilestoneStatus.QUEUED -> return true
                }
                return false
            }
            AdaptiveRouteOffsetMilestoneStatus.QUEUED -> return next == AdaptiveRouteOffsetMilestoneStatus.DONE || next == AdaptiveRouteOffsetMilestoneStatus.FAILED
            AdaptiveRouteOffsetMilestoneStatus.RUNNING -> {
                when (next) {
                    AdaptiveRouteOffsetMilestoneStatus.FAILED -> return true
                }
                return false
            }
            AdaptiveRouteOffsetMilestoneStatus.DONE -> return next == AdaptiveRouteOffsetMilestoneStatus.FAILED
        }
        return false
    }

    fun moveTo(next: AdaptiveRouteOffsetMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("adaptive route offset milestone $code cannot move from $status to $next")
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

    fun projectedTotal(): Double = round((maximum + loadKg) * 100) / 100

    fun snapshot(): AdaptiveRouteOffsetMilestone {
        return AdaptiveRouteOffsetMilestone(
            code = code,
            title = title,
            depot = depot,
            ownerId = ownerId,
            budget = budget,
            loadKg = loadKg,
            attributes = attributes,
            unit = unit,
            checksum = checksum,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a adaptive route offset milestone in status new. */
fun newAdaptiveRouteOffsetMilestone(code: String, title: String): AdaptiveRouteOffsetMilestone {
    return AdaptiveRouteOffsetMilestone(
        code = code,
        title = title,
        depot = "",
        ownerId = "",
        budget = 0.0,
        loadKg = 0.0,
        attributes = mutableMapOf<String, String>(),
        unit = "",
        checksum = "",
        maximum = 0.0,
        status = AdaptiveRouteOffsetMilestoneStatus.NEW,
    )
}
