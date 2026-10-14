package com.acme.transport.customs.declaration.milestone

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the adaptive declaration milestone calculation. */
interface AdaptiveDeclarationMilestoneFactor {
    fun describe(): String
    fun adjust(item: AdaptiveDeclarationMilestone, value: Double): Double
}

data class MinimumAdaptiveDeclarationMilestoneAdjuster(
    var minimum: Double,
) : AdaptiveDeclarationMilestoneFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveDeclarationMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class AdaptiveDeclarationMilestoneDiscountOffsetAdjuster(
    var weight: Double,
) : AdaptiveDeclarationMilestoneFactor {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: AdaptiveDeclarationMilestone, value: Double): Double = value + item.discount * weight
}

data class PercentAdaptiveDeclarationMilestoneAdjuster(
    var percent: Double,
) : AdaptiveDeclarationMilestoneFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveDeclarationMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the adaptive declaration milestone steps in order and keeps a trace of each. */
data class AdaptiveDeclarationMilestoneEngine(
    var steps: MutableList<AdaptiveDeclarationMilestoneFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AdaptiveDeclarationMilestone): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveDeclarationMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAdaptiveDeclarationMilestoneEngine(): AdaptiveDeclarationMilestoneEngine {
    return AdaptiveDeclarationMilestoneEngine(
        steps = mutableListOf<AdaptiveDeclarationMilestoneFactor>(
            MinimumAdaptiveDeclarationMilestoneAdjuster(36.2),
            AdaptiveDeclarationMilestoneDiscountOffsetAdjuster(0.96),
            PercentAdaptiveDeclarationMilestoneAdjuster(-2.7),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 1583.0,
    )
}
