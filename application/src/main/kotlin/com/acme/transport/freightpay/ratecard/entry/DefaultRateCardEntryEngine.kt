package com.acme.transport.freightpay.ratecard.entry

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the default rate card entry calculation. */
interface DefaultRateCardEntryAdjuster {
    fun describe(): String
    fun adjust(item: DefaultRateCardEntry, value: Double): Double
}

data class PercentDefaultRateCardEntryAdjuster(var percent: Double) : DefaultRateCardEntryAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultRateCardEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingDefaultRateCardEntryAdjuster(var step: Double) : DefaultRateCardEntryAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultRateCardEntry, value: Double): Double = roundTo(value, step)
}

data class CapDefaultRateCardEntryAdjuster(var cap: Double) : DefaultRateCardEntryAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultRateCardEntry, value: Double): Double = minOf(value, cap)
}

/** Runs the default rate card entry steps in order and keeps a trace of each. */
data class DefaultRateCardEntryEngine(
    var steps: MutableList<DefaultRateCardEntryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefaultRateCardEntry): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultRateCardEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefaultRateCardEntryEngine(): DefaultRateCardEntryEngine {
    return DefaultRateCardEntryEngine(
        steps = mutableListOf<DefaultRateCardEntryAdjuster>(
            PercentDefaultRateCardEntryAdjuster(-14.2),
            RoundingDefaultRateCardEntryAdjuster(1.0),
            CapDefaultRateCardEntryAdjuster(11553.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 36519.7,
    )
}
