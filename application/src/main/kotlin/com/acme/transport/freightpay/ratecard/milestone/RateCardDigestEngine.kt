package com.acme.transport.freightpay.ratecard.milestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rate card digest calculation. */
interface RateCardDigestFactor {
    fun describe(): String
    fun adjust(item: RateCardDigest, value: Double): Double
}

data class CapRateCardDigestAdjuster(var cap: Double) : RateCardDigestFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RateCardDigest, value: Double): Double = minOf(value, cap)
}

data class RateCardDigestCeilingOffsetAdjuster(var weight: Double) : RateCardDigestFactor {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: RateCardDigest, value: Double): Double = value + item.ceiling * weight
}

data class FlaggedRateCardDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RateCardDigestFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RateCardDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumRateCardDigestAdjuster(var minimum: Double) : RateCardDigestFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RateCardDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingRateCardDigestAdjuster(var step: Double) : RateCardDigestFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RateCardDigest, value: Double): Double = roundTo(value, step)
}

data class PercentRateCardDigestAdjuster(var percent: Double) : RateCardDigestFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RateCardDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the rate card digest steps in order and keeps a trace of each. */
data class RateCardDigestEngine(
    var steps: MutableList<RateCardDigestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RateCardDigest): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RateCardDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRateCardDigestEngine(flags: Registry): RateCardDigestEngine {
    return RateCardDigestEngine(
        steps = mutableListOf<RateCardDigestFactor>(
            CapRateCardDigestAdjuster(13274.0),
            RateCardDigestCeilingOffsetAdjuster(0.39),
            FlaggedRateCardDigestAdjuster(flags, "rate-card-digest-manual", 9.1),
            MinimumRateCardDigestAdjuster(25.6),
            RoundingRateCardDigestAdjuster(0.05),
            PercentRateCardDigestAdjuster(4.3),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 45781.4,
    )
}
