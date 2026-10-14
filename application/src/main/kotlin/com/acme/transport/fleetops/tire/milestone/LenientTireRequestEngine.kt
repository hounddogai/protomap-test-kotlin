package com.acme.transport.fleetops.tire.milestone

import com.acme.transport.kit.flags.Registry

/** One step of the lenient tire request calculation. */
interface LenientTireRequestStep {
    fun describe(): String
    fun adjust(item: LenientTireRequest, value: Double): Double
}

data class FlaggedLenientTireRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientTireRequestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientTireRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumLenientTireRequestAdjuster(var minimum: Double) : LenientTireRequestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientTireRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CountryLenientTireRequestAdjuster(var factors: MutableMap<String, Double>) : LenientTireRequestStep {
    override fun describe(): String = "by country"

    override fun adjust(item: LenientTireRequest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the lenient tire request steps in order and keeps a trace of each. */
data class LenientTireRequestEngine(
    var steps: MutableList<LenientTireRequestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LenientTireRequest): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientTireRequestStep = steps[index]
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

    fun evaluateAll(items: MutableList<LenientTireRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLenientTireRequestEngine(flags: Registry): LenientTireRequestEngine {
    return LenientTireRequestEngine(
        steps = mutableListOf<LenientTireRequestStep>(
            FlaggedLenientTireRequestAdjuster(flags, "lenient-tire-request-rolling", 2.3),
            MinimumLenientTireRequestAdjuster(28.8),
            CountryLenientTireRequestAdjuster(
                mutableMapOf<String, Double>("south" to 0.89, "central" to 1.04, "east" to 1.37),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 2367.7,
    )
}
