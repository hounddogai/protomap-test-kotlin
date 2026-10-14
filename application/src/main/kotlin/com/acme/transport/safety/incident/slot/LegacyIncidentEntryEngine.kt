package com.acme.transport.safety.incident.slot

import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy incident entry calculation. */
interface LegacyIncidentEntryStep {
    fun describe(): String
    fun adjust(item: LegacyIncidentEntry, value: Double): Double
}

data class RoundingLegacyIncidentEntryAdjuster(var step: Double) : LegacyIncidentEntryStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyIncidentEntry, value: Double): Double = roundTo(value, step)
}

data class TieredLegacyIncidentEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyIncidentEntryStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyIncidentEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentLegacyIncidentEntryAdjuster(var percent: Double) : LegacyIncidentEntryStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyIncidentEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LegacyIncidentEntryQuantityOffsetAdjuster(var weight: Double) : LegacyIncidentEntryStep {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: LegacyIncidentEntry, value: Double): Double = value + item.quantity.toDouble() * weight
}

data class CountryLegacyIncidentEntryAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyIncidentEntryStep {
    override fun describe(): String = "by country"

    override fun adjust(item: LegacyIncidentEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapLegacyIncidentEntryAdjuster(var cap: Double) : LegacyIncidentEntryStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyIncidentEntry, value: Double): Double = minOf(value, cap)
}

/** Runs the legacy incident entry steps in order and keeps a trace of each. */
data class LegacyIncidentEntryEngine(
    var steps: MutableList<LegacyIncidentEntryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LegacyIncidentEntry): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyIncidentEntryStep = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyIncidentEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLegacyIncidentEntryEngine(): LegacyIncidentEntryEngine {
    return LegacyIncidentEntryEngine(
        steps = mutableListOf<LegacyIncidentEntryStep>(
            RoundingLegacyIncidentEntryAdjuster(0.05),
            TieredLegacyIncidentEntryAdjuster(
                mutableListOf<Double>(2570.0, 6392.0, 6659.0),
                mutableListOf<Double>(1.0, 0.86, 0.9),
            ),
            PercentLegacyIncidentEntryAdjuster(-21.4),
            LegacyIncidentEntryQuantityOffsetAdjuster(1.47),
            CountryLegacyIncidentEntryAdjuster(mutableMapOf<String, Double>("west" to 0.91)),
            CapLegacyIncidentEntryAdjuster(16703.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 21359.9,
    )
}
