package com.acme.transport.network.transittime.timeoverride

import com.acme.transport.kit.numbers.roundTo

/** One step of the default transit time exception calculation. */
interface DefaultTransitTimeExceptionModifier {
    fun describe(): String
    fun adjust(item: DefaultTransitTimeException, value: Double): Double
}

data class CapDefaultTransitTimeExceptionAdjuster(var cap: Double) : DefaultTransitTimeExceptionModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultTransitTimeException, value: Double): Double = minOf(value, cap)
}

data class PercentDefaultTransitTimeExceptionAdjuster(
    var percent: Double,
) : DefaultTransitTimeExceptionModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultTransitTimeException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingDefaultTransitTimeExceptionAdjuster(var step: Double) : DefaultTransitTimeExceptionModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultTransitTimeException, value: Double): Double = roundTo(value, step)
}

/** Runs the default transit time exception steps in order and keeps a trace of each. */
data class DefaultTransitTimeExceptionEngine(
    var steps: MutableList<DefaultTransitTimeExceptionModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DefaultTransitTimeException): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultTransitTimeExceptionModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultTransitTimeException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDefaultTransitTimeExceptionEngine(): DefaultTransitTimeExceptionEngine {
    return DefaultTransitTimeExceptionEngine(
        steps = mutableListOf<DefaultTransitTimeExceptionModifier>(
            CapDefaultTransitTimeExceptionAdjuster(35602.0),
            PercentDefaultTransitTimeExceptionAdjuster(21.9),
            RoundingDefaultTransitTimeExceptionAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 69292.5,
    )
}
