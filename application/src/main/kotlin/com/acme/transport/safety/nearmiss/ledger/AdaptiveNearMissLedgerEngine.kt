package com.acme.transport.safety.nearmiss.ledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive near miss ledger calculation. */
interface AdaptiveNearMissLedgerModifier {
    fun describe(): String
    fun adjust(item: AdaptiveNearMissLedger, value: Double): Double
}

data class MinimumAdaptiveNearMissLedgerAdjuster(var minimum: Double) : AdaptiveNearMissLedgerModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveNearMissLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LocaleAdaptiveNearMissLedgerAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveNearMissLedgerModifier {
    override fun describe(): String = "by locale"

    override fun adjust(item: AdaptiveNearMissLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingAdaptiveNearMissLedgerAdjuster(var step: Double) : AdaptiveNearMissLedgerModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveNearMissLedger, value: Double): Double = roundTo(value, step)
}

data class PercentAdaptiveNearMissLedgerAdjuster(var percent: Double) : AdaptiveNearMissLedgerModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveNearMissLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapAdaptiveNearMissLedgerAdjuster(var cap: Double) : AdaptiveNearMissLedgerModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveNearMissLedger, value: Double): Double = minOf(value, cap)
}

data class FlaggedAdaptiveNearMissLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveNearMissLedgerModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveNearMissLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the adaptive near miss ledger steps in order and keeps a trace of each. */
data class AdaptiveNearMissLedgerEngine(
    var steps: MutableList<AdaptiveNearMissLedgerModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AdaptiveNearMissLedger): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveNearMissLedgerModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveNearMissLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAdaptiveNearMissLedgerEngine(flags: Registry): AdaptiveNearMissLedgerEngine {
    return AdaptiveNearMissLedgerEngine(
        steps = mutableListOf<AdaptiveNearMissLedgerModifier>(
            MinimumAdaptiveNearMissLedgerAdjuster(26.6),
            LocaleAdaptiveNearMissLedgerAdjuster(mutableMapOf<String, Double>("south" to 0.94)),
            RoundingAdaptiveNearMissLedgerAdjuster(0.05),
            PercentAdaptiveNearMissLedgerAdjuster(4.2),
            CapAdaptiveNearMissLedgerAdjuster(26384.0),
            FlaggedAdaptiveNearMissLedgerAdjuster(flags, "adaptive-near-miss-ledger-weighted", 7.6),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 21836.7,
    )
}
