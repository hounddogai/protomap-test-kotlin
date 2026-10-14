package com.acme.transport.freightpay.ratecard.cardledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the global rate card record calculation. */
interface GlobalRateCardRecordStep {
    fun describe(): String
    fun adjust(item: GlobalRateCardRecord, value: Double): Double
}

data class CapGlobalRateCardRecordAdjuster(var cap: Double) : GlobalRateCardRecordStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalRateCardRecord, value: Double): Double = minOf(value, cap)
}

data class MinimumGlobalRateCardRecordAdjuster(var minimum: Double) : GlobalRateCardRecordStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalRateCardRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingGlobalRateCardRecordAdjuster(var step: Double) : GlobalRateCardRecordStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalRateCardRecord, value: Double): Double = roundTo(value, step)
}

data class TieredGlobalRateCardRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalRateCardRecordStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalRateCardRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedGlobalRateCardRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalRateCardRecordStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalRateCardRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the global rate card record steps in order and keeps a trace of each. */
data class GlobalRateCardRecordEngine(
    var steps: MutableList<GlobalRateCardRecordStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: GlobalRateCardRecord): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalRateCardRecordStep = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalRateCardRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultGlobalRateCardRecordEngine(flags: Registry): GlobalRateCardRecordEngine {
    return GlobalRateCardRecordEngine(
        steps = mutableListOf<GlobalRateCardRecordStep>(
            CapGlobalRateCardRecordAdjuster(48051.0),
            MinimumGlobalRateCardRecordAdjuster(41.8),
            RoundingGlobalRateCardRecordAdjuster(0.5),
            TieredGlobalRateCardRecordAdjuster(
                mutableListOf<Double>(2244.0, 9660.0, 9978.0),
                mutableListOf<Double>(1.01, 0.92, 0.99),
            ),
            FlaggedGlobalRateCardRecordAdjuster(flags, "global-rate-card-record-regional", 6.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 19593.5,
    )
}
