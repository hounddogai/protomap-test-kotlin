package com.acme.transport.network.hub.primaryhubsummary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped hub draft calculation. */
interface CappedHubDraftFactor {
    fun describe(): String
    fun adjust(item: CappedHubDraft, value: Double): Double
}

data class RoundingCappedHubDraftAdjuster(var step: Double) : CappedHubDraftFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedHubDraft, value: Double): Double = roundTo(value, step)
}

data class TieredCappedHubDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedHubDraftFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedHubDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapCappedHubDraftAdjuster(var cap: Double) : CappedHubDraftFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedHubDraft, value: Double): Double = minOf(value, cap)
}

data class PercentCappedHubDraftAdjuster(var percent: Double) : CappedHubDraftFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedHubDraft, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CappedHubDraftQuantityOffsetAdjuster(var weight: Double) : CappedHubDraftFactor {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: CappedHubDraft, value: Double): Double = value + item.quantity.toDouble() * weight
}

data class FlaggedCappedHubDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedHubDraftFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedHubDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the capped hub draft steps in order and keeps a trace of each. */
data class CappedHubDraftEngine(
    var steps: MutableList<CappedHubDraftFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CappedHubDraft): Double {
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

    fun evaluateAll(items: MutableList<CappedHubDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCappedHubDraftEngine(flags: Registry): CappedHubDraftEngine {
    return CappedHubDraftEngine(
        steps = mutableListOf<CappedHubDraftFactor>(
            RoundingCappedHubDraftAdjuster(0.1),
            TieredCappedHubDraftAdjuster(
                mutableListOf<Double>(855.0, 6411.0, 9196.0, 9473.0),
                mutableListOf<Double>(1.14, 0.9, 1.07, 0.96),
            ),
            CapCappedHubDraftAdjuster(16116.0),
            PercentCappedHubDraftAdjuster(17.6),
            CappedHubDraftQuantityOffsetAdjuster(1.44),
            FlaggedCappedHubDraftAdjuster(flags, "capped-hub-draft-regional", 6.4),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 52875.5,
    )
}
