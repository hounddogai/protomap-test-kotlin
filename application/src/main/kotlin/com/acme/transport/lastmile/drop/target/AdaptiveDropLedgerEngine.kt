package com.acme.transport.lastmile.drop.target

/** One step of the adaptive drop ledger calculation. */
interface AdaptiveDropLedgerModifier {
    fun describe(): String
    fun adjust(item: AdaptiveDropLedger, value: Double): Double
}

data class CapAdaptiveDropLedgerAdjuster(var cap: Double) : AdaptiveDropLedgerModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveDropLedger, value: Double): Double = minOf(value, cap)
}

data class MinimumAdaptiveDropLedgerAdjuster(var minimum: Double) : AdaptiveDropLedgerModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveDropLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentAdaptiveDropLedgerAdjuster(var percent: Double) : AdaptiveDropLedgerModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveDropLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the adaptive drop ledger steps in order and keeps a trace of each. */
data class AdaptiveDropLedgerEngine(
    var steps: MutableList<AdaptiveDropLedgerModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AdaptiveDropLedger): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveDropLedgerModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveDropLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAdaptiveDropLedgerEngine(): AdaptiveDropLedgerEngine {
    return AdaptiveDropLedgerEngine(
        steps = mutableListOf<AdaptiveDropLedgerModifier>(
            CapAdaptiveDropLedgerAdjuster(8174.0),
            MinimumAdaptiveDropLedgerAdjuster(39.8),
            PercentAdaptiveDropLedgerAdjuster(-11.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 86258.8,
    )
}
