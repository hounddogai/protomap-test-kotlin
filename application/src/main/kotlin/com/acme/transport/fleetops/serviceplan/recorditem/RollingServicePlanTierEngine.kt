package com.acme.transport.fleetops.serviceplan.recorditem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling service plan tier calculation. */
interface RollingServicePlanTierStep {
    fun describe(): String
    fun adjust(item: RollingServicePlanTier, value: Double): Double
}

data class RoundingRollingServicePlanTierAdjuster(var step: Double) : RollingServicePlanTierStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingServicePlanTier, value: Double): Double = roundTo(value, step)
}

data class FlaggedRollingServicePlanTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingServicePlanTierStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingServicePlanTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelRollingServicePlanTierAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingServicePlanTierStep {
    override fun describe(): String = "by label"

    override fun adjust(item: RollingServicePlanTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RollingServicePlanTierLoadKgOffsetAdjuster(var weight: Double) : RollingServicePlanTierStep {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: RollingServicePlanTier, value: Double): Double = value + item.loadKg * weight
}

/** Runs the rolling service plan tier steps in order and keeps a trace of each. */
data class RollingServicePlanTierEngine(
    var steps: MutableList<RollingServicePlanTierStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RollingServicePlanTier): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingServicePlanTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRollingServicePlanTierEngine(flags: Registry): RollingServicePlanTierEngine {
    return RollingServicePlanTierEngine(
        steps = mutableListOf<RollingServicePlanTierStep>(
            RoundingRollingServicePlanTierAdjuster(0.5),
            FlaggedRollingServicePlanTierAdjuster(flags, "rolling-service-plan-tier-primary", 8.5),
            LabelRollingServicePlanTierAdjuster(mutableMapOf<String, Double>("central" to 1.15, "east" to 1.37)),
            RollingServicePlanTierLoadKgOffsetAdjuster(0.14),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 37599.2,
    )
}
