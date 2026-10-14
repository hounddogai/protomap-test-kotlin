package com.acme.transport.emissions.fuelburn.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled fuel burn plan records of emissions's fuel burn work, and the rules, stores, and calculations
// around them.

/** Where a scheduled fuel burn plan is in its life cycle. */
enum class ScheduledFuelBurnPlanStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A scheduled fuel burn plan as the fuel burn team keeps it. */
data class ScheduledFuelBurnPlan(
    var code: String,
    var label: String,
    var source: String,
    var depot: String,
    var summary: String,
    var batchSize: Int,
    var tags: MutableList<String>,
    var minimum: Double,
    var etaMinutes: Int,
    var region: String,
    var status: ScheduledFuelBurnPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledFuelBurnPlanStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledFuelBurnPlanStatus): Boolean {
        when (status) {
            ScheduledFuelBurnPlanStatus.REQUESTED -> return next == ScheduledFuelBurnPlanStatus.EXPIRED
            ScheduledFuelBurnPlanStatus.GRANTED -> {
                when (next) {
                    ScheduledFuelBurnPlanStatus.REVOKED, ScheduledFuelBurnPlanStatus.EXPIRED -> return true
                }
                return false
            }
            ScheduledFuelBurnPlanStatus.REVOKED -> {
                when (next) {
                    ScheduledFuelBurnPlanStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledFuelBurnPlanStatus) {
        if (!canMoveTo(next)) {
            error("scheduled fuel burn plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"scheduled fuel burn plan"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedValue(): Double = round(batchSize.toDouble() * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): ScheduledFuelBurnPlan {
        return ScheduledFuelBurnPlan(
            code = code,
            label = label,
            source = source,
            depot = depot,
            summary = summary,
            batchSize = batchSize,
            tags = tags,
            minimum = minimum,
            etaMinutes = etaMinutes,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a scheduled fuel burn plan in status requested. */
fun newScheduledFuelBurnPlan(code: String, label: String): ScheduledFuelBurnPlan {
    return ScheduledFuelBurnPlan(
        code = code,
        label = label,
        source = "",
        depot = "",
        summary = "",
        batchSize = 0,
        tags = mutableListOf<String>(),
        minimum = 0.0,
        etaMinutes = 0,
        region = "",
        status = ScheduledFuelBurnPlanStatus.REQUESTED,
    )
}
