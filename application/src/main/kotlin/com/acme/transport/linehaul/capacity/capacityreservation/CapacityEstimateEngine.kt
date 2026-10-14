package com.acme.transport.linehaul.capacity.capacityreservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the capacity estimate calculation. */
interface CapacityEstimateAdjuster {
    fun describe(): String
    fun adjust(item: CapacityEstimate, value: Double): Double
}

data class MinimumCapacityEstimateAdjuster(var minimum: Double) : CapacityEstimateAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CapacityEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentCapacityEstimateAdjuster(var percent: Double) : CapacityEstimateAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CapacityEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingCapacityEstimateAdjuster(var step: Double) : CapacityEstimateAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CapacityEstimate, value: Double): Double = roundTo(value, step)
}

data class CapCapacityEstimateAdjuster(var cap: Double) : CapacityEstimateAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CapacityEstimate, value: Double): Double = minOf(value, cap)
}

data class FlaggedCapacityEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CapacityEstimateAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CapacityEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapacityEstimateSequenceOffsetAdjuster(var weight: Double) : CapacityEstimateAdjuster {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: CapacityEstimate, value: Double): Double = value + item.sequence.toDouble() * weight
}

/** Runs the capacity estimate steps in order and keeps a trace of each. */
data class CapacityEstimateEngine(
    var steps: MutableList<CapacityEstimateAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CapacityEstimate): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CapacityEstimateAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<CapacityEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCapacityEstimateEngine(flags: Registry): CapacityEstimateEngine {
    return CapacityEstimateEngine(
        steps = mutableListOf<CapacityEstimateAdjuster>(
            MinimumCapacityEstimateAdjuster(3.9),
            PercentCapacityEstimateAdjuster(-6.1),
            RoundingCapacityEstimateAdjuster(5.0),
            CapCapacityEstimateAdjuster(48580.0),
            FlaggedCapacityEstimateAdjuster(flags, "capacity-estimate-weighted", 8.9),
            CapacityEstimateSequenceOffsetAdjuster(0.41),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 81818.7,
    )
}
