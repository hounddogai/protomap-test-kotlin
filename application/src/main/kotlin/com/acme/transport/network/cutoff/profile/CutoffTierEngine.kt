package com.acme.transport.network.cutoff.profile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the cutoff tier calculation. */
interface CutoffTierStep {
    fun describe(): String
    fun adjust(item: CutoffTier, value: Double): Double
}

data class RoundingCutoffTierAdjuster(var step: Double) : CutoffTierStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CutoffTier, value: Double): Double = roundTo(value, step)
}

data class FlaggedCutoffTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CutoffTierStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CutoffTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelCutoffTierAdjuster(var factors: MutableMap<String, Double>) : CutoffTierStep {
    override fun describe(): String = "by label"

    override fun adjust(item: CutoffTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CutoffTierScoreOffsetAdjuster(var weight: Double) : CutoffTierStep {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: CutoffTier, value: Double): Double = value + item.score * weight
}

data class PercentCutoffTierAdjuster(var percent: Double) : CutoffTierStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CutoffTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the cutoff tier steps in order and keeps a trace of each. */
data class CutoffTierEngine(
    var steps: MutableList<CutoffTierStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CutoffTier): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CutoffTierStep = steps[index]
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

    fun evaluateAll(items: MutableList<CutoffTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCutoffTierEngine(flags: Registry): CutoffTierEngine {
    return CutoffTierEngine(
        steps = mutableListOf<CutoffTierStep>(
            RoundingCutoffTierAdjuster(0.25),
            FlaggedCutoffTierAdjuster(flags, "cutoff-tier-batched", 2.4),
            LabelCutoffTierAdjuster(mutableMapOf<String, Double>("central" to 1.29)),
            CutoffTierScoreOffsetAdjuster(0.27),
            PercentCutoffTierAdjuster(-18.7),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 92803.1,
    )
}
