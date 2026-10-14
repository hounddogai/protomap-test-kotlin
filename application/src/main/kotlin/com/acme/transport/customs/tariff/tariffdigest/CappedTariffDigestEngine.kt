package com.acme.transport.customs.tariff.tariffdigest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capped tariff digest calculation. */
interface CappedTariffDigestFactor {
    fun describe(): String
    fun adjust(item: CappedTariffDigest, value: Double): Double
}

data class TieredCappedTariffDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedTariffDigestFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedTariffDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentCappedTariffDigestAdjuster(var percent: Double) : CappedTariffDigestFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedTariffDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapCappedTariffDigestAdjuster(var cap: Double) : CappedTariffDigestFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedTariffDigest, value: Double): Double = minOf(value, cap)
}

data class FlaggedCappedTariffDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedTariffDigestFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedTariffDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LaneCappedTariffDigestAdjuster(var factors: MutableMap<String, Double>) : CappedTariffDigestFactor {
    override fun describe(): String = "by lane"

    override fun adjust(item: CappedTariffDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the capped tariff digest steps in order and keeps a trace of each. */
data class CappedTariffDigestEngine(
    var steps: MutableList<CappedTariffDigestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedTariffDigest): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedTariffDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedTariffDigestEngine(flags: Registry): CappedTariffDigestEngine {
    return CappedTariffDigestEngine(
        steps = mutableListOf<CappedTariffDigestFactor>(
            TieredCappedTariffDigestAdjuster(
                mutableListOf<Double>(1734.0, 6579.0, 8306.0),
                mutableListOf<Double>(1.12, 1.14, 1.1),
            ),
            PercentCappedTariffDigestAdjuster(4.1),
            CapCappedTariffDigestAdjuster(28122.0),
            FlaggedCappedTariffDigestAdjuster(flags, "capped-tariff-digest-adaptive", 0.1),
            LaneCappedTariffDigestAdjuster(mutableMapOf<String, Double>("central" to 1.0, "west" to 1.07)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 32604.5,
    )
}
