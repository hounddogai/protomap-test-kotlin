package com.acme.transport.network.transittime.timerequest

import com.acme.transport.kit.flags.Registry

/** One step of the lenient transit time request calculation. */
interface LenientTransitTimeRequestFactor {
    fun describe(): String
    fun adjust(item: LenientTransitTimeRequest, value: Double): Double
}

data class MinimumLenientTransitTimeRequestAdjuster(var minimum: Double) : LenientTransitTimeRequestFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientTransitTimeRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedLenientTransitTimeRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientTransitTimeRequestFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientTransitTimeRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentLenientTransitTimeRequestAdjuster(var percent: Double) : LenientTransitTimeRequestFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientTransitTimeRequest, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the lenient transit time request steps in order and keeps a trace of each. */
data class LenientTransitTimeRequestEngine(
    var steps: MutableList<LenientTransitTimeRequestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LenientTransitTimeRequest): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientTransitTimeRequestFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LenientTransitTimeRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLenientTransitTimeRequestEngine(flags: Registry): LenientTransitTimeRequestEngine {
    return LenientTransitTimeRequestEngine(
        steps = mutableListOf<LenientTransitTimeRequestFactor>(
            MinimumLenientTransitTimeRequestAdjuster(23.6),
            FlaggedLenientTransitTimeRequestAdjuster(flags, "lenient-transit-time-request-rolling", -8.7),
            PercentLenientTransitTimeRequestAdjuster(-19.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 46687.6,
    )
}
