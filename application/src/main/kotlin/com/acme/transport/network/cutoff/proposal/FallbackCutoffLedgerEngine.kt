package com.acme.transport.network.cutoff.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fallback cutoff ledger calculation. */
interface FallbackCutoffLedgerModifier {
    fun describe(): String
    fun adjust(item: FallbackCutoffLedger, value: Double): Double
}

data class RoundingFallbackCutoffLedgerAdjuster(var step: Double) : FallbackCutoffLedgerModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackCutoffLedger, value: Double): Double = roundTo(value, step)
}

data class CapFallbackCutoffLedgerAdjuster(var cap: Double) : FallbackCutoffLedgerModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackCutoffLedger, value: Double): Double = minOf(value, cap)
}

data class TieredFallbackCutoffLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackCutoffLedgerModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackCutoffLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedFallbackCutoffLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackCutoffLedgerModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackCutoffLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentFallbackCutoffLedgerAdjuster(var percent: Double) : FallbackCutoffLedgerModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackCutoffLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FallbackCutoffLedgerEtaMinutesOffsetAdjuster(var weight: Double) : FallbackCutoffLedgerModifier {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: FallbackCutoffLedger, value: Double): Double {
        return value + item.etaMinutes.toDouble() * weight
    }
}

/** Runs the fallback cutoff ledger steps in order and keeps a trace of each. */
data class FallbackCutoffLedgerEngine(
    var steps: MutableList<FallbackCutoffLedgerModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackCutoffLedger): Double {
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

    fun evaluateAll(items: MutableList<FallbackCutoffLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackCutoffLedgerEngine(flags: Registry): FallbackCutoffLedgerEngine {
    return FallbackCutoffLedgerEngine(
        steps = mutableListOf<FallbackCutoffLedgerModifier>(
            RoundingFallbackCutoffLedgerAdjuster(1.0),
            CapFallbackCutoffLedgerAdjuster(36428.0),
            TieredFallbackCutoffLedgerAdjuster(
                mutableListOf<Double>(3515.0, 8026.0),
                mutableListOf<Double>(0.94, 1.16),
            ),
            FlaggedFallbackCutoffLedgerAdjuster(flags, "fallback-cutoff-ledger-global", 6.9),
            PercentFallbackCutoffLedgerAdjuster(-15.0),
            FallbackCutoffLedgerEtaMinutesOffsetAdjuster(0.52),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 11464.6,
    )
}
