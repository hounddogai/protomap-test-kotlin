package com.acme.transport.lastmile.redelivery.redeliveryentry

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the regional redelivery entry calculation. */
interface RegionalRedeliveryEntryStep {
    fun describe(): String
    fun adjust(item: RegionalRedeliveryEntry, value: Double): Double
}

data class RoundingRegionalRedeliveryEntryAdjuster(var step: Double) : RegionalRedeliveryEntryStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalRedeliveryEntry, value: Double): Double = roundTo(value, step)
}

data class TieredRegionalRedeliveryEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalRedeliveryEntryStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalRedeliveryEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentRegionalRedeliveryEntryAdjuster(var percent: Double) : RegionalRedeliveryEntryStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalRedeliveryEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the regional redelivery entry steps in order and keeps a trace of each. */
data class RegionalRedeliveryEntryEngine(
    var steps: MutableList<RegionalRedeliveryEntryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalRedeliveryEntry): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalRedeliveryEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalRedeliveryEntryEngine(): RegionalRedeliveryEntryEngine {
    return RegionalRedeliveryEntryEngine(
        steps = mutableListOf<RegionalRedeliveryEntryStep>(
            RoundingRegionalRedeliveryEntryAdjuster(1.0),
            TieredRegionalRedeliveryEntryAdjuster(
                mutableListOf<Double>(1497.0, 8147.0),
                mutableListOf<Double>(1.11, 1.01),
            ),
            PercentRegionalRedeliveryEntryAdjuster(16.9),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 50754.2,
    )
}
