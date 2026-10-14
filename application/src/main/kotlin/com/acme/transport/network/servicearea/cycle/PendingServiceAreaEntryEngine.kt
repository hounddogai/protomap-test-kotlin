package com.acme.transport.network.servicearea.cycle

import com.acme.transport.kit.numbers.roundTo

/** One step of the pending service area entry calculation. */
interface PendingServiceAreaEntryModifier {
    fun describe(): String
    fun adjust(item: PendingServiceAreaEntry, value: Double): Double
}

data class MinimumPendingServiceAreaEntryAdjuster(var minimum: Double) : PendingServiceAreaEntryModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingServiceAreaEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingPendingServiceAreaEntryAdjuster(var step: Double) : PendingServiceAreaEntryModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingServiceAreaEntry, value: Double): Double = roundTo(value, step)
}

data class TieredPendingServiceAreaEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingServiceAreaEntryModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingServiceAreaEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentPendingServiceAreaEntryAdjuster(var percent: Double) : PendingServiceAreaEntryModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingServiceAreaEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the pending service area entry steps in order and keeps a trace of each. */
data class PendingServiceAreaEntryEngine(
    var steps: MutableList<PendingServiceAreaEntryModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingServiceAreaEntry): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingServiceAreaEntryModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PendingServiceAreaEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingServiceAreaEntryEngine(): PendingServiceAreaEntryEngine {
    return PendingServiceAreaEntryEngine(
        steps = mutableListOf<PendingServiceAreaEntryModifier>(
            MinimumPendingServiceAreaEntryAdjuster(39.4),
            RoundingPendingServiceAreaEntryAdjuster(1.0),
            TieredPendingServiceAreaEntryAdjuster(
                mutableListOf<Double>(2705.0, 4028.0, 4420.0, 6846.0),
                mutableListOf<Double>(0.97, 0.97, 1.01, 0.93),
            ),
            PercentPendingServiceAreaEntryAdjuster(20.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 46134.6,
    )
}
