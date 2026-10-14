package com.acme.transport.terminal.dwell.dwellreview

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the legacy dwell milestone calculation. */
interface LegacyDwellMilestoneAdjuster {
    fun describe(): String
    fun adjust(item: LegacyDwellMilestone, value: Double): Double
}

data class RoundingLegacyDwellMilestoneAdjuster(var step: Double) : LegacyDwellMilestoneAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyDwellMilestone, value: Double): Double = roundTo(value, step)
}

data class MinimumLegacyDwellMilestoneAdjuster(var minimum: Double) : LegacyDwellMilestoneAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyDwellMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentLegacyDwellMilestoneAdjuster(var percent: Double) : LegacyDwellMilestoneAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyDwellMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredLegacyDwellMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyDwellMilestoneAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyDwellMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the legacy dwell milestone steps in order and keeps a trace of each. */
data class LegacyDwellMilestoneEngine(
    var steps: MutableList<LegacyDwellMilestoneAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LegacyDwellMilestone): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyDwellMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLegacyDwellMilestoneEngine(): LegacyDwellMilestoneEngine {
    return LegacyDwellMilestoneEngine(
        steps = mutableListOf<LegacyDwellMilestoneAdjuster>(
            RoundingLegacyDwellMilestoneAdjuster(5.0),
            MinimumLegacyDwellMilestoneAdjuster(34.8),
            PercentLegacyDwellMilestoneAdjuster(-14.2),
            TieredLegacyDwellMilestoneAdjuster(
                mutableListOf<Double>(1804.0, 4447.0, 7387.0, 9965.0),
                mutableListOf<Double>(1.13, 1.16, 0.96, 0.97),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 14575.0,
    )
}
