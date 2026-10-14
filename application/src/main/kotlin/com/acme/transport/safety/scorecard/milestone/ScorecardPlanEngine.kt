package com.acme.transport.safety.scorecard.milestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the scorecard plan calculation. */
interface ScorecardPlanAdjuster {
    fun describe(): String
    fun adjust(item: ScorecardPlan, value: Double): Double
}

data class RoundingScorecardPlanAdjuster(var step: Double) : ScorecardPlanAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScorecardPlan, value: Double): Double = roundTo(value, step)
}

data class CapScorecardPlanAdjuster(var cap: Double) : ScorecardPlanAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScorecardPlan, value: Double): Double = minOf(value, cap)
}

data class ScorecardPlanMaximumOffsetAdjuster(var weight: Double) : ScorecardPlanAdjuster {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: ScorecardPlan, value: Double): Double = value + item.maximum * weight
}

data class FlaggedScorecardPlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScorecardPlanAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScorecardPlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumScorecardPlanAdjuster(var minimum: Double) : ScorecardPlanAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScorecardPlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelScorecardPlanAdjuster(var factors: MutableMap<String, Double>) : ScorecardPlanAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: ScorecardPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the scorecard plan steps in order and keeps a trace of each. */
data class ScorecardPlanEngine(
    var steps: MutableList<ScorecardPlanAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ScorecardPlan): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScorecardPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultScorecardPlanEngine(flags: Registry): ScorecardPlanEngine {
    return ScorecardPlanEngine(
        steps = mutableListOf<ScorecardPlanAdjuster>(
            RoundingScorecardPlanAdjuster(0.05),
            CapScorecardPlanAdjuster(15851.0),
            ScorecardPlanMaximumOffsetAdjuster(1.78),
            FlaggedScorecardPlanAdjuster(flags, "scorecard-plan-regional", 3.2),
            MinimumScorecardPlanAdjuster(10.3),
            LabelScorecardPlanAdjuster(mutableMapOf<String, Double>("north" to 1.08, "south" to 0.91)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 3721.2,
    )
}
