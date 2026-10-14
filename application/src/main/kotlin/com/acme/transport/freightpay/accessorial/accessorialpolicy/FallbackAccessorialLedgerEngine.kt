package com.acme.transport.freightpay.accessorial.accessorialpolicy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fallback accessorial ledger calculation. */
interface FallbackAccessorialLedgerAdjuster {
    fun describe(): String
    fun adjust(item: FallbackAccessorialLedger, value: Double): Double
}

data class LabelFallbackAccessorialLedgerAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackAccessorialLedgerAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: FallbackAccessorialLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredFallbackAccessorialLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackAccessorialLedgerAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackAccessorialLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedFallbackAccessorialLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackAccessorialLedgerAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackAccessorialLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentFallbackAccessorialLedgerAdjuster(var percent: Double) : FallbackAccessorialLedgerAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackAccessorialLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the fallback accessorial ledger steps in order and keeps a trace of each. */
data class FallbackAccessorialLedgerEngine(
    var steps: MutableList<FallbackAccessorialLedgerAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FallbackAccessorialLedger): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackAccessorialLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFallbackAccessorialLedgerEngine(flags: Registry): FallbackAccessorialLedgerEngine {
    return FallbackAccessorialLedgerEngine(
        steps = mutableListOf<FallbackAccessorialLedgerAdjuster>(
            LabelFallbackAccessorialLedgerAdjuster(
                mutableMapOf<String, Double>("central" to 1.39, "east" to 0.86),
            ),
            TieredFallbackAccessorialLedgerAdjuster(
                mutableListOf<Double>(2526.0, 8910.0),
                mutableListOf<Double>(1.19, 0.85),
            ),
            FlaggedFallbackAccessorialLedgerAdjuster(flags, "fallback-accessorial-ledger-weighted", 8.1),
            PercentFallbackAccessorialLedgerAdjuster(-23.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 79238.7,
    )
}
