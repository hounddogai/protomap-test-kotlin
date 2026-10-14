package com.acme.transport.fleetops.defect.bundle

import com.acme.transport.kit.flags.Registry

/** One step of the scheduled defect claim calculation. */
interface ScheduledDefectClaimFactor {
    fun describe(): String
    fun adjust(item: ScheduledDefectClaim, value: Double): Double
}

data class FlaggedScheduledDefectClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledDefectClaimFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledDefectClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TitleScheduledDefectClaimAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledDefectClaimFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: ScheduledDefectClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumScheduledDefectClaimAdjuster(var minimum: Double) : ScheduledDefectClaimFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledDefectClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the scheduled defect claim steps in order and keeps a trace of each. */
data class ScheduledDefectClaimEngine(
    var steps: MutableList<ScheduledDefectClaimFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledDefectClaim): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledDefectClaimFactor = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledDefectClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledDefectClaimEngine(flags: Registry): ScheduledDefectClaimEngine {
    return ScheduledDefectClaimEngine(
        steps = mutableListOf<ScheduledDefectClaimFactor>(
            FlaggedScheduledDefectClaimAdjuster(flags, "scheduled-defect-claim-strict", 4.9),
            TitleScheduledDefectClaimAdjuster(
                mutableMapOf<String, Double>("west" to 0.84, "north" to 0.92, "retail" to 0.7),
            ),
            MinimumScheduledDefectClaimAdjuster(26.4),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 68044.2,
    )
}
