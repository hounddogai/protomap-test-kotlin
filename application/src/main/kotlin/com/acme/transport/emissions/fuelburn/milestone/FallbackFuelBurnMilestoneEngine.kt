package com.acme.transport.emissions.fuelburn.milestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback fuel burn milestone calculation. */
interface FallbackFuelBurnMilestoneModifier {
    fun describe(): String
    fun adjust(item: FallbackFuelBurnMilestone, value: Double): Double
}

data class FlaggedFallbackFuelBurnMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackFuelBurnMilestoneModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackFuelBurnMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingFallbackFuelBurnMilestoneAdjuster(var step: Double) : FallbackFuelBurnMilestoneModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackFuelBurnMilestone, value: Double): Double = roundTo(value, step)
}

data class CapFallbackFuelBurnMilestoneAdjuster(var cap: Double) : FallbackFuelBurnMilestoneModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackFuelBurnMilestone, value: Double): Double = minOf(value, cap)
}

data class FallbackFuelBurnMilestoneLoadKgOffsetAdjuster(
    var weight: Double,
) : FallbackFuelBurnMilestoneModifier {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: FallbackFuelBurnMilestone, value: Double): Double = value + item.loadKg * weight
}

data class PercentFallbackFuelBurnMilestoneAdjuster(var percent: Double) : FallbackFuelBurnMilestoneModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackFuelBurnMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumFallbackFuelBurnMilestoneAdjuster(var minimum: Double) : FallbackFuelBurnMilestoneModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackFuelBurnMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the fallback fuel burn milestone steps in order and keeps a trace of each. */
data class FallbackFuelBurnMilestoneEngine(
    var steps: MutableList<FallbackFuelBurnMilestoneModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackFuelBurnMilestone): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackFuelBurnMilestoneModifier = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackFuelBurnMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackFuelBurnMilestoneEngine(flags: Registry): FallbackFuelBurnMilestoneEngine {
    return FallbackFuelBurnMilestoneEngine(
        steps = mutableListOf<FallbackFuelBurnMilestoneModifier>(
            FlaggedFallbackFuelBurnMilestoneAdjuster(flags, "fallback-fuel-burn-milestone-capped", -4.4),
            RoundingFallbackFuelBurnMilestoneAdjuster(0.25),
            CapFallbackFuelBurnMilestoneAdjuster(1442.0),
            FallbackFuelBurnMilestoneLoadKgOffsetAdjuster(0.83),
            PercentFallbackFuelBurnMilestoneAdjuster(-19.0),
            MinimumFallbackFuelBurnMilestoneAdjuster(17.7),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 71463.9,
    )
}
