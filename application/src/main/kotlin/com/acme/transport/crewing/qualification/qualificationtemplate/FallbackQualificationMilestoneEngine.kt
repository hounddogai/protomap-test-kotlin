package com.acme.transport.crewing.qualification.qualificationtemplate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback qualification milestone calculation. */
interface FallbackQualificationMilestoneStep {
    fun describe(): String
    fun adjust(item: FallbackQualificationMilestone, value: Double): Double
}

data class PercentFallbackQualificationMilestoneAdjuster(
    var percent: Double,
) : FallbackQualificationMilestoneStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackQualificationMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumFallbackQualificationMilestoneAdjuster(
    var minimum: Double,
) : FallbackQualificationMilestoneStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackQualificationMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedFallbackQualificationMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackQualificationMilestoneStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackQualificationMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredFallbackQualificationMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackQualificationMilestoneStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackQualificationMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapFallbackQualificationMilestoneAdjuster(var cap: Double) : FallbackQualificationMilestoneStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackQualificationMilestone, value: Double): Double = minOf(value, cap)
}

data class RoundingFallbackQualificationMilestoneAdjuster(
    var step: Double,
) : FallbackQualificationMilestoneStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackQualificationMilestone, value: Double): Double = roundTo(value, step)
}

/** Runs the fallback qualification milestone steps in order and keeps a trace of each. */
data class FallbackQualificationMilestoneEngine(
    var steps: MutableList<FallbackQualificationMilestoneStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FallbackQualificationMilestone): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackQualificationMilestoneStep = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackQualificationMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFallbackQualificationMilestoneEngine(flags: Registry): FallbackQualificationMilestoneEngine {
    return FallbackQualificationMilestoneEngine(
        steps = mutableListOf<FallbackQualificationMilestoneStep>(
            PercentFallbackQualificationMilestoneAdjuster(-14.2),
            MinimumFallbackQualificationMilestoneAdjuster(0.3),
            FlaggedFallbackQualificationMilestoneAdjuster(
                flags,
                "fallback-qualification-milestone-scheduled",
                4.2,
            ),
            TieredFallbackQualificationMilestoneAdjuster(
                mutableListOf<Double>(32.0, 1694.0),
                mutableListOf<Double>(1.02, 0.82),
            ),
            CapFallbackQualificationMilestoneAdjuster(41632.0),
            RoundingFallbackQualificationMilestoneAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 14027.8,
    )
}
