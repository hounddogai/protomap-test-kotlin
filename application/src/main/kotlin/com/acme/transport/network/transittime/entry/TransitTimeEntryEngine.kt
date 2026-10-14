package com.acme.transport.network.transittime.entry

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the transit time entry calculation. */
interface TransitTimeEntryModifier {
    fun describe(): String
    fun adjust(item: TransitTimeEntry, value: Double): Double
}

data class TieredTransitTimeEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TransitTimeEntryModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TransitTimeEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapTransitTimeEntryAdjuster(var cap: Double) : TransitTimeEntryModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TransitTimeEntry, value: Double): Double = minOf(value, cap)
}

data class PercentTransitTimeEntryAdjuster(var percent: Double) : TransitTimeEntryModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TransitTimeEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the transit time entry steps in order and keeps a trace of each. */
data class TransitTimeEntryEngine(
    var steps: MutableList<TransitTimeEntryModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TransitTimeEntry): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TransitTimeEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTransitTimeEntryEngine(): TransitTimeEntryEngine {
    return TransitTimeEntryEngine(
        steps = mutableListOf<TransitTimeEntryModifier>(
            TieredTransitTimeEntryAdjuster(
                mutableListOf<Double>(3548.0, 9158.0),
                mutableListOf<Double>(1.1, 1.14),
            ),
            CapTransitTimeEntryAdjuster(15782.0),
            PercentTransitTimeEntryAdjuster(4.6),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 25348.0,
    )
}
