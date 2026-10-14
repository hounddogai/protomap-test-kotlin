package com.acme.transport.safety.scorecard.profile

import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled scorecard mapping calculation. */
interface ScheduledScorecardMappingAdjuster {
    fun describe(): String
    fun adjust(item: ScheduledScorecardMapping, value: Double): Double
}

data class RoundingScheduledScorecardMappingAdjuster(var step: Double) : ScheduledScorecardMappingAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledScorecardMapping, value: Double): Double = roundTo(value, step)
}

data class TieredScheduledScorecardMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledScorecardMappingAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledScorecardMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class NameScheduledScorecardMappingAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledScorecardMappingAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: ScheduledScorecardMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the scheduled scorecard mapping steps in order and keeps a trace of each. */
data class ScheduledScorecardMappingEngine(
    var steps: MutableList<ScheduledScorecardMappingAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledScorecardMapping): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledScorecardMappingAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledScorecardMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledScorecardMappingEngine(): ScheduledScorecardMappingEngine {
    return ScheduledScorecardMappingEngine(
        steps = mutableListOf<ScheduledScorecardMappingAdjuster>(
            RoundingScheduledScorecardMappingAdjuster(0.1),
            TieredScheduledScorecardMappingAdjuster(
                mutableListOf<Double>(1485.0, 5624.0, 8064.0),
                mutableListOf<Double>(0.8, 1.16, 1.07),
            ),
            NameScheduledScorecardMappingAdjuster(mutableMapOf<String, Double>("east" to 0.82)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 65408.8,
    )
}
