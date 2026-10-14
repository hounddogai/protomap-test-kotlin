package com.acme.transport.linehaul.backhaul.backhaulallocation

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive backhaul milestone calculation. */
interface AdaptiveBackhaulMilestoneModifier {
    fun describe(): String
    fun adjust(item: AdaptiveBackhaulMilestone, value: Double): Double
}

data class TieredAdaptiveBackhaulMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveBackhaulMilestoneModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveBackhaulMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class AdaptiveBackhaulMilestonePriorityOffsetAdjuster(
    var weight: Double,
) : AdaptiveBackhaulMilestoneModifier {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: AdaptiveBackhaulMilestone, value: Double): Double {
        return value + item.priority.toDouble() * weight
    }
}

data class PercentAdaptiveBackhaulMilestoneAdjuster(var percent: Double) : AdaptiveBackhaulMilestoneModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveBackhaulMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumAdaptiveBackhaulMilestoneAdjuster(var minimum: Double) : AdaptiveBackhaulMilestoneModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveBackhaulMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingAdaptiveBackhaulMilestoneAdjuster(var step: Double) : AdaptiveBackhaulMilestoneModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveBackhaulMilestone, value: Double): Double = roundTo(value, step)
}

/** Runs the adaptive backhaul milestone steps in order and keeps a trace of each. */
data class AdaptiveBackhaulMilestoneEngine(
    var steps: MutableList<AdaptiveBackhaulMilestoneModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AdaptiveBackhaulMilestone): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveBackhaulMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAdaptiveBackhaulMilestoneEngine(): AdaptiveBackhaulMilestoneEngine {
    return AdaptiveBackhaulMilestoneEngine(
        steps = mutableListOf<AdaptiveBackhaulMilestoneModifier>(
            TieredAdaptiveBackhaulMilestoneAdjuster(
                mutableListOf<Double>(1423.0, 3403.0, 5180.0, 9458.0),
                mutableListOf<Double>(0.9, 0.95, 1.13, 0.89),
            ),
            AdaptiveBackhaulMilestonePriorityOffsetAdjuster(1.96),
            PercentAdaptiveBackhaulMilestoneAdjuster(10.2),
            MinimumAdaptiveBackhaulMilestoneAdjuster(16.6),
            RoundingAdaptiveBackhaulMilestoneAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 56827.1,
    )
}
