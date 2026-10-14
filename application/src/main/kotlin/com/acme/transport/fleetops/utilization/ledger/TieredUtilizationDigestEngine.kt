package com.acme.transport.fleetops.utilization.ledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tiered utilization digest calculation. */
interface TieredUtilizationDigestAdjuster {
    fun describe(): String
    fun adjust(item: TieredUtilizationDigest, value: Double): Double
}

data class CapTieredUtilizationDigestAdjuster(var cap: Double) : TieredUtilizationDigestAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredUtilizationDigest, value: Double): Double = minOf(value, cap)
}

data class FlaggedTieredUtilizationDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredUtilizationDigestAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredUtilizationDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumTieredUtilizationDigestAdjuster(var minimum: Double) : TieredUtilizationDigestAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredUtilizationDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredUtilizationDigestMaximumOffsetAdjuster(var weight: Double) : TieredUtilizationDigestAdjuster {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: TieredUtilizationDigest, value: Double): Double = value + item.maximum * weight
}

data class TieredTieredUtilizationDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredUtilizationDigestAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredUtilizationDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the tiered utilization digest steps in order and keeps a trace of each. */
data class TieredUtilizationDigestEngine(
    var steps: MutableList<TieredUtilizationDigestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TieredUtilizationDigest): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredUtilizationDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTieredUtilizationDigestEngine(flags: Registry): TieredUtilizationDigestEngine {
    return TieredUtilizationDigestEngine(
        steps = mutableListOf<TieredUtilizationDigestAdjuster>(
            CapTieredUtilizationDigestAdjuster(34340.0),
            FlaggedTieredUtilizationDigestAdjuster(flags, "tiered-utilization-digest-weighted", 8.7),
            MinimumTieredUtilizationDigestAdjuster(13.3),
            TieredUtilizationDigestMaximumOffsetAdjuster(1.42),
            TieredTieredUtilizationDigestAdjuster(
                mutableListOf<Double>(639.0, 915.0),
                mutableListOf<Double>(1.03, 1.19),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 59371.6,
    )
}
