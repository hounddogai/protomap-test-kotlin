package com.acme.transport.safety.incident.proposal

import com.acme.transport.kit.numbers.roundTo

/** One step of the incident tier calculation. */
interface IncidentTierModifier {
    fun describe(): String
    fun adjust(item: IncidentTier, value: Double): Double
}

data class IncidentTierCeilingOffsetAdjuster(var weight: Double) : IncidentTierModifier {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: IncidentTier, value: Double): Double = value + item.ceiling * weight
}

data class TieredIncidentTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : IncidentTierModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: IncidentTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingIncidentTierAdjuster(var step: Double) : IncidentTierModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: IncidentTier, value: Double): Double = roundTo(value, step)
}

/** Runs the incident tier steps in order and keeps a trace of each. */
data class IncidentTierEngine(
    var steps: MutableList<IncidentTierModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: IncidentTier): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: IncidentTierModifier = steps[index]
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

    fun evaluateAll(items: MutableList<IncidentTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultIncidentTierEngine(): IncidentTierEngine {
    return IncidentTierEngine(
        steps = mutableListOf<IncidentTierModifier>(
            IncidentTierCeilingOffsetAdjuster(0.69),
            TieredIncidentTierAdjuster(mutableListOf<Double>(4059.0, 7559.0), mutableListOf<Double>(0.98, 0.82)),
            RoundingIncidentTierAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 20756.4,
    )
}
