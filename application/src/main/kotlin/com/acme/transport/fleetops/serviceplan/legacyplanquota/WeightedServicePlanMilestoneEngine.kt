package com.acme.transport.fleetops.serviceplan.legacyplanquota

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the weighted service plan milestone calculation. */
interface WeightedServicePlanMilestoneModifier {
    fun describe(): String
    fun adjust(item: WeightedServicePlanMilestone, value: Double): Double
}

data class PercentWeightedServicePlanMilestoneAdjuster(
    var percent: Double,
) : WeightedServicePlanMilestoneModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedServicePlanMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class WeightedServicePlanMilestoneDistanceKmOffsetAdjuster(
    var weight: Double,
) : WeightedServicePlanMilestoneModifier {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: WeightedServicePlanMilestone, value: Double): Double = value + item.distanceKm * weight
}

data class FlaggedWeightedServicePlanMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedServicePlanMilestoneModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedServicePlanMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelWeightedServicePlanMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedServicePlanMilestoneModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: WeightedServicePlanMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingWeightedServicePlanMilestoneAdjuster(
    var step: Double,
) : WeightedServicePlanMilestoneModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedServicePlanMilestone, value: Double): Double = roundTo(value, step)
}

data class MinimumWeightedServicePlanMilestoneAdjuster(
    var minimum: Double,
) : WeightedServicePlanMilestoneModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedServicePlanMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the weighted service plan milestone steps in order and keeps a trace of each. */
data class WeightedServicePlanMilestoneEngine(
    var steps: MutableList<WeightedServicePlanMilestoneModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: WeightedServicePlanMilestone): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedServicePlanMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultWeightedServicePlanMilestoneEngine(flags: Registry): WeightedServicePlanMilestoneEngine {
    return WeightedServicePlanMilestoneEngine(
        steps = mutableListOf<WeightedServicePlanMilestoneModifier>(
            PercentWeightedServicePlanMilestoneAdjuster(23.4),
            WeightedServicePlanMilestoneDistanceKmOffsetAdjuster(0.29),
            FlaggedWeightedServicePlanMilestoneAdjuster(flags, "weighted-service-plan-milestone-capped", 9.0),
            LabelWeightedServicePlanMilestoneAdjuster(
                mutableMapOf<String, Double>("central" to 1.31, "retail" to 1.38, "south" to 1.07),
            ),
            RoundingWeightedServicePlanMilestoneAdjuster(5.0),
            MinimumWeightedServicePlanMilestoneAdjuster(35.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 56224.0,
    )
}
