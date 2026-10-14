package com.acme.transport.terminal.dwell.dwellmilestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fallback dwell milestone calculation. */
interface FallbackDwellMilestoneModifier {
    fun describe(): String
    fun adjust(item: FallbackDwellMilestone, value: Double): Double
}

data class PercentFallbackDwellMilestoneAdjuster(var percent: Double) : FallbackDwellMilestoneModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackDwellMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedFallbackDwellMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackDwellMilestoneModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackDwellMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredFallbackDwellMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackDwellMilestoneModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackDwellMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingFallbackDwellMilestoneAdjuster(var step: Double) : FallbackDwellMilestoneModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackDwellMilestone, value: Double): Double = roundTo(value, step)
}

/** Runs the fallback dwell milestone steps in order and keeps a trace of each. */
data class FallbackDwellMilestoneEngine(
    var steps: MutableList<FallbackDwellMilestoneModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackDwellMilestone): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackDwellMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackDwellMilestoneEngine(flags: Registry): FallbackDwellMilestoneEngine {
    return FallbackDwellMilestoneEngine(
        steps = mutableListOf<FallbackDwellMilestoneModifier>(
            PercentFallbackDwellMilestoneAdjuster(0.7),
            FlaggedFallbackDwellMilestoneAdjuster(flags, "fallback-dwell-milestone-weighted", -7.1),
            TieredFallbackDwellMilestoneAdjuster(
                mutableListOf<Double>(1152.0, 7405.0),
                mutableListOf<Double>(0.93, 0.91),
            ),
            RoundingFallbackDwellMilestoneAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 37859.1,
    )
}
