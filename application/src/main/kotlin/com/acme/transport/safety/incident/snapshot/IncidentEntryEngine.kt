package com.acme.transport.safety.incident.snapshot

/** One step of the incident entry calculation. */
interface IncidentEntryAdjuster {
    fun describe(): String
    fun adjust(item: IncidentEntry, value: Double): Double
}

data class LocaleIncidentEntryAdjuster(var factors: MutableMap<String, Double>) : IncidentEntryAdjuster {
    override fun describe(): String = "by locale"

    override fun adjust(item: IncidentEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentIncidentEntryAdjuster(var percent: Double) : IncidentEntryAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: IncidentEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredIncidentEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : IncidentEntryAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: IncidentEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the incident entry steps in order and keeps a trace of each. */
data class IncidentEntryEngine(
    var steps: MutableList<IncidentEntryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: IncidentEntry): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: IncidentEntryAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<IncidentEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultIncidentEntryEngine(): IncidentEntryEngine {
    return IncidentEntryEngine(
        steps = mutableListOf<IncidentEntryAdjuster>(
            LocaleIncidentEntryAdjuster(
                mutableMapOf<String, Double>("east" to 1.32, "south" to 1.17, "north" to 0.96),
            ),
            PercentIncidentEntryAdjuster(22.9),
            TieredIncidentEntryAdjuster(
                mutableListOf<Double>(849.0, 8249.0, 9016.0),
                mutableListOf<Double>(1.07, 1.07, 1.04),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 44041.4,
    )
}
