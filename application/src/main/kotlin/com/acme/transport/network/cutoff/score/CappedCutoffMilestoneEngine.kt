package com.acme.transport.network.cutoff.score

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the capped cutoff milestone calculation. */
interface CappedCutoffMilestoneStep {
    fun describe(): String
    fun adjust(item: CappedCutoffMilestone, value: Double): Double
}

data class PercentCappedCutoffMilestoneAdjuster(var percent: Double) : CappedCutoffMilestoneStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedCutoffMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingCappedCutoffMilestoneAdjuster(var step: Double) : CappedCutoffMilestoneStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedCutoffMilestone, value: Double): Double = roundTo(value, step)
}

data class TieredCappedCutoffMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedCutoffMilestoneStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedCutoffMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedCappedCutoffMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedCutoffMilestoneStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedCutoffMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CountryCappedCutoffMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedCutoffMilestoneStep {
    override fun describe(): String = "by country"

    override fun adjust(item: CappedCutoffMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapCappedCutoffMilestoneAdjuster(var cap: Double) : CappedCutoffMilestoneStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedCutoffMilestone, value: Double): Double = minOf(value, cap)
}

/** Runs the capped cutoff milestone steps in order and keeps a trace of each. */
data class CappedCutoffMilestoneEngine(
    var steps: MutableList<CappedCutoffMilestoneStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CappedCutoffMilestone): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedCutoffMilestoneStep = steps[index]
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

    fun evaluateAll(items: MutableList<CappedCutoffMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCappedCutoffMilestoneEngine(flags: Registry): CappedCutoffMilestoneEngine {
    return CappedCutoffMilestoneEngine(
        steps = mutableListOf<CappedCutoffMilestoneStep>(
            PercentCappedCutoffMilestoneAdjuster(-9.5),
            RoundingCappedCutoffMilestoneAdjuster(0.25),
            TieredCappedCutoffMilestoneAdjuster(
                mutableListOf<Double>(77.0, 637.0),
                mutableListOf<Double>(1.15, 1.05),
            ),
            FlaggedCappedCutoffMilestoneAdjuster(flags, "capped-cutoff-milestone-tiered", 7.9),
            CountryCappedCutoffMilestoneAdjuster(mutableMapOf<String, Double>("south" to 1.09)),
            CapCappedCutoffMilestoneAdjuster(15447.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 62414.6,
    )
}
