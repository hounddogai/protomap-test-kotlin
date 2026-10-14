package com.acme.transport.customs.tariff.milestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive tariff milestone calculation. */
interface AdaptiveTariffMilestoneAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveTariffMilestone, value: Double): Double
}

data class MinimumAdaptiveTariffMilestoneAdjuster(var minimum: Double) : AdaptiveTariffMilestoneAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveTariffMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapAdaptiveTariffMilestoneAdjuster(var cap: Double) : AdaptiveTariffMilestoneAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveTariffMilestone, value: Double): Double = minOf(value, cap)
}

data class RoundingAdaptiveTariffMilestoneAdjuster(var step: Double) : AdaptiveTariffMilestoneAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveTariffMilestone, value: Double): Double = roundTo(value, step)
}

data class PercentAdaptiveTariffMilestoneAdjuster(var percent: Double) : AdaptiveTariffMilestoneAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveTariffMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedAdaptiveTariffMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveTariffMilestoneAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveTariffMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class AdaptiveTariffMilestoneUpdatedAtOffsetAdjuster(
    var weight: Double,
) : AdaptiveTariffMilestoneAdjuster {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: AdaptiveTariffMilestone, value: Double): Double {
        return value + item.updatedAt.toDouble() * weight
    }
}

/** Runs the adaptive tariff milestone steps in order and keeps a trace of each. */
data class AdaptiveTariffMilestoneEngine(
    var steps: MutableList<AdaptiveTariffMilestoneAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AdaptiveTariffMilestone): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveTariffMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAdaptiveTariffMilestoneEngine(flags: Registry): AdaptiveTariffMilestoneEngine {
    return AdaptiveTariffMilestoneEngine(
        steps = mutableListOf<AdaptiveTariffMilestoneAdjuster>(
            MinimumAdaptiveTariffMilestoneAdjuster(42.9),
            CapAdaptiveTariffMilestoneAdjuster(17818.0),
            RoundingAdaptiveTariffMilestoneAdjuster(0.1),
            PercentAdaptiveTariffMilestoneAdjuster(-3.7),
            FlaggedAdaptiveTariffMilestoneAdjuster(flags, "adaptive-tariff-milestone-seasonal", -10.0),
            AdaptiveTariffMilestoneUpdatedAtOffsetAdjuster(0.27),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 30031.8,
    )
}
