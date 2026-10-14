package com.acme.transport.linehaul.trailer.request

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the trailer request calculation. */
interface TrailerRequestStep {
    fun describe(): String
    fun adjust(item: TrailerRequest, value: Double): Double
}

data class RoundingTrailerRequestAdjuster(var step: Double) : TrailerRequestStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TrailerRequest, value: Double): Double = roundTo(value, step)
}

data class CountryTrailerRequestAdjuster(var factors: MutableMap<String, Double>) : TrailerRequestStep {
    override fun describe(): String = "by country"

    override fun adjust(item: TrailerRequest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumTrailerRequestAdjuster(var minimum: Double) : TrailerRequestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TrailerRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentTrailerRequestAdjuster(var percent: Double) : TrailerRequestStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TrailerRequest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedTrailerRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TrailerRequestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TrailerRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapTrailerRequestAdjuster(var cap: Double) : TrailerRequestStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TrailerRequest, value: Double): Double = minOf(value, cap)
}

/** Runs the trailer request steps in order and keeps a trace of each. */
data class TrailerRequestEngine(
    var steps: MutableList<TrailerRequestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TrailerRequest): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TrailerRequestStep = steps[index]
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

    fun evaluateAll(items: MutableList<TrailerRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTrailerRequestEngine(flags: Registry): TrailerRequestEngine {
    return TrailerRequestEngine(
        steps = mutableListOf<TrailerRequestStep>(
            RoundingTrailerRequestAdjuster(5.0),
            CountryTrailerRequestAdjuster(mutableMapOf<String, Double>("west" to 1.34)),
            MinimumTrailerRequestAdjuster(39.1),
            PercentTrailerRequestAdjuster(-6.8),
            FlaggedTrailerRequestAdjuster(flags, "trailer-request-batched", 3.4),
            CapTrailerRequestAdjuster(41638.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 59728.1,
    )
}
