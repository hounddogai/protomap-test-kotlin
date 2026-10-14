package com.acme.transport.network.zone.batchedzonemapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the zone variance calculation. */
interface ZoneVarianceFactor {
    fun describe(): String
    fun adjust(item: ZoneVariance, value: Double): Double
}

data class FlaggedZoneVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ZoneVarianceFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ZoneVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentZoneVarianceAdjuster(var percent: Double) : ZoneVarianceFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ZoneVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapZoneVarianceAdjuster(var cap: Double) : ZoneVarianceFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ZoneVariance, value: Double): Double = minOf(value, cap)
}

data class MinimumZoneVarianceAdjuster(var minimum: Double) : ZoneVarianceFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ZoneVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingZoneVarianceAdjuster(var step: Double) : ZoneVarianceFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ZoneVariance, value: Double): Double = roundTo(value, step)
}

data class TieredZoneVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ZoneVarianceFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ZoneVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the zone variance steps in order and keeps a trace of each. */
data class ZoneVarianceEngine(
    var steps: MutableList<ZoneVarianceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ZoneVariance): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ZoneVarianceFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ZoneVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultZoneVarianceEngine(flags: Registry): ZoneVarianceEngine {
    return ZoneVarianceEngine(
        steps = mutableListOf<ZoneVarianceFactor>(
            FlaggedZoneVarianceAdjuster(flags, "zone-variance-regional", 5.5),
            PercentZoneVarianceAdjuster(5.9),
            CapZoneVarianceAdjuster(9499.0),
            MinimumZoneVarianceAdjuster(42.8),
            RoundingZoneVarianceAdjuster(5.0),
            TieredZoneVarianceAdjuster(
                mutableListOf<Double>(2528.0, 2541.0, 6465.0, 6610.0),
                mutableListOf<Double>(1.1, 1.0, 1.03, 1.14),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 52136.0,
    )
}
