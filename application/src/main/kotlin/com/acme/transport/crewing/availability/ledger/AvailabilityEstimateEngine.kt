package com.acme.transport.crewing.availability.ledger

import com.acme.transport.kit.flags.Registry

/** One step of the availability estimate calculation. */
interface AvailabilityEstimateModifier {
    fun describe(): String
    fun adjust(item: AvailabilityEstimate, value: Double): Double
}

data class AvailabilityEstimateLoadKgOffsetAdjuster(var weight: Double) : AvailabilityEstimateModifier {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: AvailabilityEstimate, value: Double): Double = value + item.loadKg * weight
}

data class TieredAvailabilityEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AvailabilityEstimateModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AvailabilityEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumAvailabilityEstimateAdjuster(var minimum: Double) : AvailabilityEstimateModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AvailabilityEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedAvailabilityEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AvailabilityEstimateModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AvailabilityEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the availability estimate steps in order and keeps a trace of each. */
data class AvailabilityEstimateEngine(
    var steps: MutableList<AvailabilityEstimateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AvailabilityEstimate): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AvailabilityEstimateModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AvailabilityEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAvailabilityEstimateEngine(flags: Registry): AvailabilityEstimateEngine {
    return AvailabilityEstimateEngine(
        steps = mutableListOf<AvailabilityEstimateModifier>(
            AvailabilityEstimateLoadKgOffsetAdjuster(1.14),
            TieredAvailabilityEstimateAdjuster(
                mutableListOf<Double>(228.0, 5158.0, 7498.0, 9722.0),
                mutableListOf<Double>(1.14, 0.97, 0.84, 0.85),
            ),
            MinimumAvailabilityEstimateAdjuster(21.7),
            FlaggedAvailabilityEstimateAdjuster(flags, "availability-estimate-strict", 9.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 43784.5,
    )
}
