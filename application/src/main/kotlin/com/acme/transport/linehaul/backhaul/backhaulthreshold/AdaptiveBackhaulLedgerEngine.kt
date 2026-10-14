package com.acme.transport.linehaul.backhaul.backhaulthreshold

import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive backhaul ledger calculation. */
interface AdaptiveBackhaulLedgerModifier {
    fun describe(): String
    fun adjust(item: AdaptiveBackhaulLedger, value: Double): Double
}

data class RoundingAdaptiveBackhaulLedgerAdjuster(var step: Double) : AdaptiveBackhaulLedgerModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveBackhaulLedger, value: Double): Double = roundTo(value, step)
}

data class AdaptiveBackhaulLedgerExpiresAtOffsetAdjuster(var weight: Double) : AdaptiveBackhaulLedgerModifier {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: AdaptiveBackhaulLedger, value: Double): Double {
        return value + item.expiresAt.toDouble() * weight
    }
}

data class CapAdaptiveBackhaulLedgerAdjuster(var cap: Double) : AdaptiveBackhaulLedgerModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveBackhaulLedger, value: Double): Double = minOf(value, cap)
}

/** Runs the adaptive backhaul ledger steps in order and keeps a trace of each. */
data class AdaptiveBackhaulLedgerEngine(
    var steps: MutableList<AdaptiveBackhaulLedgerModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AdaptiveBackhaulLedger): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveBackhaulLedgerModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveBackhaulLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAdaptiveBackhaulLedgerEngine(): AdaptiveBackhaulLedgerEngine {
    return AdaptiveBackhaulLedgerEngine(
        steps = mutableListOf<AdaptiveBackhaulLedgerModifier>(
            RoundingAdaptiveBackhaulLedgerAdjuster(0.05),
            AdaptiveBackhaulLedgerExpiresAtOffsetAdjuster(0.02),
            CapAdaptiveBackhaulLedgerAdjuster(36760.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 27240.3,
    )
}
