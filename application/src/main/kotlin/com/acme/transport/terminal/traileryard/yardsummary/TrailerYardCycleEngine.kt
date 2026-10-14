package com.acme.transport.terminal.traileryard.yardsummary

import com.acme.transport.kit.numbers.roundTo

/** One step of the trailer yard cycle calculation. */
interface TrailerYardCycleAdjuster {
    fun describe(): String
    fun adjust(item: TrailerYardCycle, value: Double): Double
}

data class PercentTrailerYardCycleAdjuster(var percent: Double) : TrailerYardCycleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TrailerYardCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TitleTrailerYardCycleAdjuster(var factors: MutableMap<String, Double>) : TrailerYardCycleAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: TrailerYardCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingTrailerYardCycleAdjuster(var step: Double) : TrailerYardCycleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TrailerYardCycle, value: Double): Double = roundTo(value, step)
}

data class TieredTrailerYardCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TrailerYardCycleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TrailerYardCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapTrailerYardCycleAdjuster(var cap: Double) : TrailerYardCycleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TrailerYardCycle, value: Double): Double = minOf(value, cap)
}

data class TrailerYardCyclePriorityOffsetAdjuster(var weight: Double) : TrailerYardCycleAdjuster {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: TrailerYardCycle, value: Double): Double = value + item.priority.toDouble() * weight
}

/** Runs the trailer yard cycle steps in order and keeps a trace of each. */
data class TrailerYardCycleEngine(
    var steps: MutableList<TrailerYardCycleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TrailerYardCycle): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TrailerYardCycleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<TrailerYardCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTrailerYardCycleEngine(): TrailerYardCycleEngine {
    return TrailerYardCycleEngine(
        steps = mutableListOf<TrailerYardCycleAdjuster>(
            PercentTrailerYardCycleAdjuster(-14.2),
            TitleTrailerYardCycleAdjuster(mutableMapOf<String, Double>("west" to 0.92, "retail" to 1.06)),
            RoundingTrailerYardCycleAdjuster(0.5),
            TieredTrailerYardCycleAdjuster(
                mutableListOf<Double>(811.0, 3480.0, 4444.0, 8610.0),
                mutableListOf<Double>(0.81, 0.81, 0.93, 0.95),
            ),
            CapTrailerYardCycleAdjuster(7823.0),
            TrailerYardCyclePriorityOffsetAdjuster(0.87),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 26577.8,
    )
}
