package com.acme.transport.network.transittime.entry

import com.acme.transport.kit.numbers.roundTo

/** One step of the transit time quota calculation. */
interface TransitTimeQuotaModifier {
    fun describe(): String
    fun adjust(item: TransitTimeQuota, value: Double): Double
}

data class TransitTimeQuotaRateOffsetAdjuster(var weight: Double) : TransitTimeQuotaModifier {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: TransitTimeQuota, value: Double): Double = value + item.rate * weight
}

data class CapTransitTimeQuotaAdjuster(var cap: Double) : TransitTimeQuotaModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TransitTimeQuota, value: Double): Double = minOf(value, cap)
}

data class MinimumTransitTimeQuotaAdjuster(var minimum: Double) : TransitTimeQuotaModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TransitTimeQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingTransitTimeQuotaAdjuster(var step: Double) : TransitTimeQuotaModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TransitTimeQuota, value: Double): Double = roundTo(value, step)
}

/** Runs the transit time quota steps in order and keeps a trace of each. */
data class TransitTimeQuotaEngine(
    var steps: MutableList<TransitTimeQuotaModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TransitTimeQuota): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TransitTimeQuotaModifier = steps[index]
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

    fun evaluateAll(items: MutableList<TransitTimeQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTransitTimeQuotaEngine(): TransitTimeQuotaEngine {
    return TransitTimeQuotaEngine(
        steps = mutableListOf<TransitTimeQuotaModifier>(
            TransitTimeQuotaRateOffsetAdjuster(0.39),
            CapTransitTimeQuotaAdjuster(27307.0),
            MinimumTransitTimeQuotaAdjuster(28.3),
            RoundingTransitTimeQuotaAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 77642.8,
    )
}
