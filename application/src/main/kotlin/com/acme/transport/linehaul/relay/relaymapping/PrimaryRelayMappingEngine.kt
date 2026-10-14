package com.acme.transport.linehaul.relay.relaymapping

import com.acme.transport.kit.numbers.roundTo

/** One step of the primary relay mapping calculation. */
interface PrimaryRelayMappingModifier {
    fun describe(): String
    fun adjust(item: PrimaryRelayMapping, value: Double): Double
}

data class RoundingPrimaryRelayMappingAdjuster(var step: Double) : PrimaryRelayMappingModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryRelayMapping, value: Double): Double = roundTo(value, step)
}

data class MinimumPrimaryRelayMappingAdjuster(var minimum: Double) : PrimaryRelayMappingModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryRelayMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredPrimaryRelayMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryRelayMappingModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryRelayMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentPrimaryRelayMappingAdjuster(var percent: Double) : PrimaryRelayMappingModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryRelayMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class PrimaryRelayMappingDistanceKmOffsetAdjuster(var weight: Double) : PrimaryRelayMappingModifier {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: PrimaryRelayMapping, value: Double): Double = value + item.distanceKm * weight
}

/** Runs the primary relay mapping steps in order and keeps a trace of each. */
data class PrimaryRelayMappingEngine(
    var steps: MutableList<PrimaryRelayMappingModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PrimaryRelayMapping): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryRelayMappingModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryRelayMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPrimaryRelayMappingEngine(): PrimaryRelayMappingEngine {
    return PrimaryRelayMappingEngine(
        steps = mutableListOf<PrimaryRelayMappingModifier>(
            RoundingPrimaryRelayMappingAdjuster(0.05),
            MinimumPrimaryRelayMappingAdjuster(10.9),
            TieredPrimaryRelayMappingAdjuster(
                mutableListOf<Double>(322.0, 447.0, 5436.0),
                mutableListOf<Double>(1.13, 0.83, 0.89),
            ),
            PercentPrimaryRelayMappingAdjuster(17.0),
            PrimaryRelayMappingDistanceKmOffsetAdjuster(0.12),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 52015.4,
    )
}
