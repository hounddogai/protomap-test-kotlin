package com.acme.transport.crewing.restbreak.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rest break milestone calculation. */
interface RestBreakMilestoneStep {
    fun describe(): String
    fun adjust(item: RestBreakMilestone, value: Double): Double
}

data class RoundingRestBreakMilestoneAdjuster(var step: Double) : RestBreakMilestoneStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RestBreakMilestone, value: Double): Double = roundTo(value, step)
}

data class RestBreakMilestoneSequenceOffsetAdjuster(var weight: Double) : RestBreakMilestoneStep {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: RestBreakMilestone, value: Double): Double = value + item.sequence.toDouble() * weight
}

data class TieredRestBreakMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RestBreakMilestoneStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RestBreakMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapRestBreakMilestoneAdjuster(var cap: Double) : RestBreakMilestoneStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RestBreakMilestone, value: Double): Double = minOf(value, cap)
}

data class FlaggedRestBreakMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RestBreakMilestoneStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RestBreakMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the rest break milestone steps in order and keeps a trace of each. */
data class RestBreakMilestoneEngine(
    var steps: MutableList<RestBreakMilestoneStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RestBreakMilestone): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RestBreakMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRestBreakMilestoneEngine(flags: Registry): RestBreakMilestoneEngine {
    return RestBreakMilestoneEngine(
        steps = mutableListOf<RestBreakMilestoneStep>(
            RoundingRestBreakMilestoneAdjuster(0.5),
            RestBreakMilestoneSequenceOffsetAdjuster(0.13),
            TieredRestBreakMilestoneAdjuster(
                mutableListOf<Double>(915.0, 2806.0, 5712.0, 7417.0),
                mutableListOf<Double>(0.9, 0.84, 1.04, 1.16),
            ),
            CapRestBreakMilestoneAdjuster(8655.0),
            FlaggedRestBreakMilestoneAdjuster(flags, "rest-break-milestone-pending", -4.4),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 83901.7,
    )
}
