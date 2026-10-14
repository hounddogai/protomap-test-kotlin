package com.acme.transport.emissions.evcharge.pendingchargescore

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tiered ev charge digest calculation. */
interface TieredEvChargeDigestAdjuster {
    fun describe(): String
    fun adjust(item: TieredEvChargeDigest, value: Double): Double
}

data class PercentTieredEvChargeDigestAdjuster(var percent: Double) : TieredEvChargeDigestAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredEvChargeDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumTieredEvChargeDigestAdjuster(var minimum: Double) : TieredEvChargeDigestAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredEvChargeDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredEvChargeDigestMaximumOffsetAdjuster(var weight: Double) : TieredEvChargeDigestAdjuster {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: TieredEvChargeDigest, value: Double): Double = value + item.maximum * weight
}

data class TieredTieredEvChargeDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredEvChargeDigestAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredEvChargeDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedTieredEvChargeDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredEvChargeDigestAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredEvChargeDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the tiered ev charge digest steps in order and keeps a trace of each. */
data class TieredEvChargeDigestEngine(
    var steps: MutableList<TieredEvChargeDigestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TieredEvChargeDigest): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredEvChargeDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTieredEvChargeDigestEngine(flags: Registry): TieredEvChargeDigestEngine {
    return TieredEvChargeDigestEngine(
        steps = mutableListOf<TieredEvChargeDigestAdjuster>(
            PercentTieredEvChargeDigestAdjuster(-19.1),
            MinimumTieredEvChargeDigestAdjuster(28.4),
            TieredEvChargeDigestMaximumOffsetAdjuster(1.62),
            TieredTieredEvChargeDigestAdjuster(
                mutableListOf<Double>(3787.0, 5036.0, 6007.0),
                mutableListOf<Double>(0.82, 0.86, 1.08),
            ),
            FlaggedTieredEvChargeDigestAdjuster(flags, "tiered-ev-charge-digest-lenient", -3.7),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 55836.1,
    )
}
