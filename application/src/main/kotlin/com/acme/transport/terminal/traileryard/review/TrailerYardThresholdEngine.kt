package com.acme.transport.terminal.traileryard.review

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the trailer yard threshold calculation. */
interface TrailerYardThresholdFactor {
    fun describe(): String
    fun adjust(item: TrailerYardThreshold, value: Double): Double
}

data class TitleTrailerYardThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : TrailerYardThresholdFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: TrailerYardThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredTrailerYardThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TrailerYardThresholdFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TrailerYardThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingTrailerYardThresholdAdjuster(var step: Double) : TrailerYardThresholdFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TrailerYardThreshold, value: Double): Double = roundTo(value, step)
}

data class FlaggedTrailerYardThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TrailerYardThresholdFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TrailerYardThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentTrailerYardThresholdAdjuster(var percent: Double) : TrailerYardThresholdFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TrailerYardThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapTrailerYardThresholdAdjuster(var cap: Double) : TrailerYardThresholdFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TrailerYardThreshold, value: Double): Double = minOf(value, cap)
}

/** Runs the trailer yard threshold steps in order and keeps a trace of each. */
data class TrailerYardThresholdEngine(
    var steps: MutableList<TrailerYardThresholdFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TrailerYardThreshold): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TrailerYardThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTrailerYardThresholdEngine(flags: Registry): TrailerYardThresholdEngine {
    return TrailerYardThresholdEngine(
        steps = mutableListOf<TrailerYardThresholdFactor>(
            TitleTrailerYardThresholdAdjuster(mutableMapOf<String, Double>("south" to 0.85, "online" to 1.17)),
            TieredTrailerYardThresholdAdjuster(
                mutableListOf<Double>(4006.0, 5068.0),
                mutableListOf<Double>(0.82, 0.99),
            ),
            RoundingTrailerYardThresholdAdjuster(5.0),
            FlaggedTrailerYardThresholdAdjuster(flags, "trailer-yard-threshold-seasonal", -4.0),
            PercentTrailerYardThresholdAdjuster(-4.1),
            CapTrailerYardThresholdAdjuster(15924.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 31096.7,
    )
}
