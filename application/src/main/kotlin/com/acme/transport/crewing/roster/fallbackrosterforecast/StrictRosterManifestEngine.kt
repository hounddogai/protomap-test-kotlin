package com.acme.transport.crewing.roster.fallbackrosterforecast

import com.acme.transport.kit.numbers.roundTo

/** One step of the strict roster manifest calculation. */
interface StrictRosterManifestStep {
    fun describe(): String
    fun adjust(item: StrictRosterManifest, value: Double): Double
}

data class RoundingStrictRosterManifestAdjuster(var step: Double) : StrictRosterManifestStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictRosterManifest, value: Double): Double = roundTo(value, step)
}

data class CountryStrictRosterManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictRosterManifestStep {
    override fun describe(): String = "by country"

    override fun adjust(item: StrictRosterManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredStrictRosterManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictRosterManifestStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictRosterManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the strict roster manifest steps in order and keeps a trace of each. */
data class StrictRosterManifestEngine(
    var steps: MutableList<StrictRosterManifestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: StrictRosterManifest): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictRosterManifestStep = steps[index]
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

    fun evaluateAll(items: MutableList<StrictRosterManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultStrictRosterManifestEngine(): StrictRosterManifestEngine {
    return StrictRosterManifestEngine(
        steps = mutableListOf<StrictRosterManifestStep>(
            RoundingStrictRosterManifestAdjuster(0.5),
            CountryStrictRosterManifestAdjuster(
                mutableMapOf<String, Double>("retail" to 0.86, "south" to 1.06, "west" to 0.97),
            ),
            TieredStrictRosterManifestAdjuster(
                mutableListOf<Double>(1872.0, 2884.0, 8810.0),
                mutableListOf<Double>(1.1, 1.0, 1.03),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 97203.2,
    )
}
