package com.acme.transport.network.transittime.timenotice

import com.acme.transport.kit.flags.Registry

/** One step of the tiered transit time variance calculation. */
interface TieredTransitTimeVarianceAdjuster {
    fun describe(): String
    fun adjust(item: TieredTransitTimeVariance, value: Double): Double
}

data class LaneTieredTransitTimeVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredTransitTimeVarianceAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: TieredTransitTimeVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredTransitTimeVarianceMinimumOffsetAdjuster(
    var weight: Double,
) : TieredTransitTimeVarianceAdjuster {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: TieredTransitTimeVariance, value: Double): Double = value + item.minimum * weight
}

data class FlaggedTieredTransitTimeVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredTransitTimeVarianceAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredTransitTimeVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredTieredTransitTimeVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredTransitTimeVarianceAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredTransitTimeVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentTieredTransitTimeVarianceAdjuster(var percent: Double) : TieredTransitTimeVarianceAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredTransitTimeVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapTieredTransitTimeVarianceAdjuster(var cap: Double) : TieredTransitTimeVarianceAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredTransitTimeVariance, value: Double): Double = minOf(value, cap)
}

/** Runs the tiered transit time variance steps in order and keeps a trace of each. */
data class TieredTransitTimeVarianceEngine(
    var steps: MutableList<TieredTransitTimeVarianceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TieredTransitTimeVariance): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredTransitTimeVarianceAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<TieredTransitTimeVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTieredTransitTimeVarianceEngine(flags: Registry): TieredTransitTimeVarianceEngine {
    return TieredTransitTimeVarianceEngine(
        steps = mutableListOf<TieredTransitTimeVarianceAdjuster>(
            LaneTieredTransitTimeVarianceAdjuster(
                mutableMapOf<String, Double>("west" to 1.08, "central" to 1.38, "online" to 0.93),
            ),
            TieredTransitTimeVarianceMinimumOffsetAdjuster(0.67),
            FlaggedTieredTransitTimeVarianceAdjuster(flags, "tiered-transit-time-variance-regional", -0.6),
            TieredTieredTransitTimeVarianceAdjuster(
                mutableListOf<Double>(8277.0, 8703.0),
                mutableListOf<Double>(0.94, 0.92),
            ),
            PercentTieredTransitTimeVarianceAdjuster(20.5),
            CapTieredTransitTimeVarianceAdjuster(11768.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 13186.6,
    )
}
