package com.acme.transport.safety.incident.incidentmanifest

import com.acme.transport.kit.numbers.roundTo

/** One step of the incident variance calculation. */
interface IncidentVarianceStep {
    fun describe(): String
    fun adjust(item: IncidentVariance, value: Double): Double
}

data class TieredIncidentVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : IncidentVarianceStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: IncidentVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumIncidentVarianceAdjuster(var minimum: Double) : IncidentVarianceStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: IncidentVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentIncidentVarianceAdjuster(var percent: Double) : IncidentVarianceStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: IncidentVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapIncidentVarianceAdjuster(var cap: Double) : IncidentVarianceStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: IncidentVariance, value: Double): Double = minOf(value, cap)
}

data class IncidentVarianceExpiresAtOffsetAdjuster(var weight: Double) : IncidentVarianceStep {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: IncidentVariance, value: Double): Double = value + item.expiresAt.toDouble() * weight
}

data class RoundingIncidentVarianceAdjuster(var step: Double) : IncidentVarianceStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: IncidentVariance, value: Double): Double = roundTo(value, step)
}

/** Runs the incident variance steps in order and keeps a trace of each. */
data class IncidentVarianceEngine(
    var steps: MutableList<IncidentVarianceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: IncidentVariance): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: IncidentVarianceStep = steps[index]
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

    fun evaluateAll(items: MutableList<IncidentVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultIncidentVarianceEngine(): IncidentVarianceEngine {
    return IncidentVarianceEngine(
        steps = mutableListOf<IncidentVarianceStep>(
            TieredIncidentVarianceAdjuster(
                mutableListOf<Double>(2507.0, 6438.0, 9474.0),
                mutableListOf<Double>(1.05, 0.97, 1.02),
            ),
            MinimumIncidentVarianceAdjuster(38.8),
            PercentIncidentVarianceAdjuster(24.5),
            CapIncidentVarianceAdjuster(30055.0),
            IncidentVarianceExpiresAtOffsetAdjuster(0.71),
            RoundingIncidentVarianceAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 33678.7,
    )
}
