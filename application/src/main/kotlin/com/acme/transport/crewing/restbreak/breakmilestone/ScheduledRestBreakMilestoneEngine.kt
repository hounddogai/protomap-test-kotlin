package com.acme.transport.crewing.restbreak.breakmilestone

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the scheduled rest break milestone calculation. */
interface ScheduledRestBreakMilestoneFactor {
    fun describe(): String
    fun adjust(item: ScheduledRestBreakMilestone, value: Double): Double
}

data class TieredScheduledRestBreakMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledRestBreakMilestoneFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledRestBreakMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RegionScheduledRestBreakMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledRestBreakMilestoneFactor {
    override fun describe(): String = "by region"

    override fun adjust(item: ScheduledRestBreakMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingScheduledRestBreakMilestoneAdjuster(var step: Double) : ScheduledRestBreakMilestoneFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledRestBreakMilestone, value: Double): Double = roundTo(value, step)
}

data class ScheduledRestBreakMilestoneDurationMinutesOffsetAdjuster(
    var weight: Double,
) : ScheduledRestBreakMilestoneFactor {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: ScheduledRestBreakMilestone, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

/** Runs the scheduled rest break milestone steps in order and keeps a trace of each. */
data class ScheduledRestBreakMilestoneEngine(
    var steps: MutableList<ScheduledRestBreakMilestoneFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledRestBreakMilestone): Double {
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

    fun evaluateAll(items: MutableList<ScheduledRestBreakMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledRestBreakMilestoneEngine(): ScheduledRestBreakMilestoneEngine {
    return ScheduledRestBreakMilestoneEngine(
        steps = mutableListOf<ScheduledRestBreakMilestoneFactor>(
            TieredScheduledRestBreakMilestoneAdjuster(
                mutableListOf<Double>(877.0, 1903.0, 7082.0),
                mutableListOf<Double>(0.89, 0.86, 1.11),
            ),
            RegionScheduledRestBreakMilestoneAdjuster(mutableMapOf<String, Double>("west" to 1.31)),
            RoundingScheduledRestBreakMilestoneAdjuster(5.0),
            ScheduledRestBreakMilestoneDurationMinutesOffsetAdjuster(0.49),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 43538.9,
    )
}
