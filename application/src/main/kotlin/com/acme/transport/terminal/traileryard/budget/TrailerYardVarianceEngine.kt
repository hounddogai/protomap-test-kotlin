package com.acme.transport.terminal.traileryard.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the trailer yard variance calculation. */
interface TrailerYardVarianceFactor {
    fun describe(): String
    fun adjust(item: TrailerYardVariance, value: Double): Double
}

data class RoundingTrailerYardVarianceAdjuster(var step: Double) : TrailerYardVarianceFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TrailerYardVariance, value: Double): Double = roundTo(value, step)
}

data class FlaggedTrailerYardVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TrailerYardVarianceFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TrailerYardVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NameTrailerYardVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : TrailerYardVarianceFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: TrailerYardVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the trailer yard variance steps in order and keeps a trace of each. */
data class TrailerYardVarianceEngine(
    var steps: MutableList<TrailerYardVarianceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TrailerYardVariance): Double {
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

    fun evaluateAll(items: MutableList<TrailerYardVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTrailerYardVarianceEngine(flags: Registry): TrailerYardVarianceEngine {
    return TrailerYardVarianceEngine(
        steps = mutableListOf<TrailerYardVarianceFactor>(
            RoundingTrailerYardVarianceAdjuster(0.5),
            FlaggedTrailerYardVarianceAdjuster(flags, "trailer-yard-variance-lenient", -3.5),
            NameTrailerYardVarianceAdjuster(mutableMapOf<String, Double>("retail" to 0.76, "online" to 1.11)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 9778.4,
    )
}
