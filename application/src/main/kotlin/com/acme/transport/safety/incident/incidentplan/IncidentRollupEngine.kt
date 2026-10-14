package com.acme.transport.safety.incident.incidentplan

import com.acme.transport.kit.numbers.roundTo

/** One step of the incident rollup calculation. */
interface IncidentRollupAdjuster {
    fun describe(): String
    fun adjust(item: IncidentRollup, value: Double): Double
}

data class TieredIncidentRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : IncidentRollupAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: IncidentRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class IncidentRollupCapacityOffsetAdjuster(var weight: Double) : IncidentRollupAdjuster {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: IncidentRollup, value: Double): Double = value + item.capacity.toDouble() * weight
}

data class CountryIncidentRollupAdjuster(var factors: MutableMap<String, Double>) : IncidentRollupAdjuster {
    override fun describe(): String = "by country"

    override fun adjust(item: IncidentRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapIncidentRollupAdjuster(var cap: Double) : IncidentRollupAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: IncidentRollup, value: Double): Double = minOf(value, cap)
}

data class RoundingIncidentRollupAdjuster(var step: Double) : IncidentRollupAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: IncidentRollup, value: Double): Double = roundTo(value, step)
}

data class PercentIncidentRollupAdjuster(var percent: Double) : IncidentRollupAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: IncidentRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the incident rollup steps in order and keeps a trace of each. */
data class IncidentRollupEngine(
    var steps: MutableList<IncidentRollupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: IncidentRollup): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: IncidentRollupAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<IncidentRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultIncidentRollupEngine(): IncidentRollupEngine {
    return IncidentRollupEngine(
        steps = mutableListOf<IncidentRollupAdjuster>(
            TieredIncidentRollupAdjuster(
                mutableListOf<Double>(1045.0, 3639.0, 7992.0, 8436.0),
                mutableListOf<Double>(0.97, 0.88, 1.06, 1.04),
            ),
            IncidentRollupCapacityOffsetAdjuster(0.61),
            CountryIncidentRollupAdjuster(mutableMapOf<String, Double>("online" to 0.83)),
            CapIncidentRollupAdjuster(40118.0),
            RoundingIncidentRollupAdjuster(0.05),
            PercentIncidentRollupAdjuster(17.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 95441.6,
    )
}
