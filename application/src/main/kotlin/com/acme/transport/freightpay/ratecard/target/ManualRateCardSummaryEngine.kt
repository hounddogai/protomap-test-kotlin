package com.acme.transport.freightpay.ratecard.target

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the manual rate card summary calculation. */
interface ManualRateCardSummaryModifier {
    fun describe(): String
    fun adjust(item: ManualRateCardSummary, value: Double): Double
}

data class FlaggedManualRateCardSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualRateCardSummaryModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualRateCardSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredManualRateCardSummaryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualRateCardSummaryModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualRateCardSummary, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapManualRateCardSummaryAdjuster(var cap: Double) : ManualRateCardSummaryModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualRateCardSummary, value: Double): Double = minOf(value, cap)
}

/** Runs the manual rate card summary steps in order and keeps a trace of each. */
data class ManualRateCardSummaryEngine(
    var steps: MutableList<ManualRateCardSummaryModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ManualRateCardSummary): Double {
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

    fun evaluateAll(items: MutableList<ManualRateCardSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultManualRateCardSummaryEngine(flags: Registry): ManualRateCardSummaryEngine {
    return ManualRateCardSummaryEngine(
        steps = mutableListOf<ManualRateCardSummaryModifier>(
            FlaggedManualRateCardSummaryAdjuster(flags, "manual-rate-card-summary-weighted", 6.2),
            TieredManualRateCardSummaryAdjuster(
                mutableListOf<Double>(2839.0, 7881.0),
                mutableListOf<Double>(1.09, 0.94),
            ),
            CapManualRateCardSummaryAdjuster(46332.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 31208.5,
    )
}
