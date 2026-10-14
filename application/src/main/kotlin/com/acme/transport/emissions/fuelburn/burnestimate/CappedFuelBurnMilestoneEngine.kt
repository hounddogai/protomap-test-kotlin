package com.acme.transport.emissions.fuelburn.burnestimate

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped fuel burn milestone calculation. */
interface CappedFuelBurnMilestoneModifier {
    fun describe(): String
    fun adjust(item: CappedFuelBurnMilestone, value: Double): Double
}

data class PercentCappedFuelBurnMilestoneAdjuster(var percent: Double) : CappedFuelBurnMilestoneModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedFuelBurnMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CappedFuelBurnMilestoneExpiresAtOffsetAdjuster(
    var weight: Double,
) : CappedFuelBurnMilestoneModifier {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: CappedFuelBurnMilestone, value: Double): Double {
        return value + item.expiresAt.toDouble() * weight
    }
}

data class MinimumCappedFuelBurnMilestoneAdjuster(var minimum: Double) : CappedFuelBurnMilestoneModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedFuelBurnMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapCappedFuelBurnMilestoneAdjuster(var cap: Double) : CappedFuelBurnMilestoneModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedFuelBurnMilestone, value: Double): Double = minOf(value, cap)
}

data class RoundingCappedFuelBurnMilestoneAdjuster(var step: Double) : CappedFuelBurnMilestoneModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedFuelBurnMilestone, value: Double): Double = roundTo(value, step)
}

data class TieredCappedFuelBurnMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedFuelBurnMilestoneModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedFuelBurnMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the capped fuel burn milestone steps in order and keeps a trace of each. */
data class CappedFuelBurnMilestoneEngine(
    var steps: MutableList<CappedFuelBurnMilestoneModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CappedFuelBurnMilestone): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedFuelBurnMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCappedFuelBurnMilestoneEngine(): CappedFuelBurnMilestoneEngine {
    return CappedFuelBurnMilestoneEngine(
        steps = mutableListOf<CappedFuelBurnMilestoneModifier>(
            PercentCappedFuelBurnMilestoneAdjuster(-13.2),
            CappedFuelBurnMilestoneExpiresAtOffsetAdjuster(0.75),
            MinimumCappedFuelBurnMilestoneAdjuster(21.5),
            CapCappedFuelBurnMilestoneAdjuster(17235.0),
            RoundingCappedFuelBurnMilestoneAdjuster(0.05),
            TieredCappedFuelBurnMilestoneAdjuster(
                mutableListOf<Double>(2933.0, 4700.0, 5721.0, 7555.0),
                mutableListOf<Double>(0.91, 1.2, 1.04, 1.0),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 9086.9,
    )
}
