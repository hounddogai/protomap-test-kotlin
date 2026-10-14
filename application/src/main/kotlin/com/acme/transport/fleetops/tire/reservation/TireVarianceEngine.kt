package com.acme.transport.fleetops.tire.reservation

import com.acme.transport.kit.flags.Registry

/** One step of the tire variance calculation. */
interface TireVarianceAdjuster {
    fun describe(): String
    fun adjust(item: TireVariance, value: Double): Double
}

data class MinimumTireVarianceAdjuster(var minimum: Double) : TireVarianceAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TireVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TireVarianceFuelLitersOffsetAdjuster(var weight: Double) : TireVarianceAdjuster {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: TireVariance, value: Double): Double = value + item.fuelLiters * weight
}

data class PercentTireVarianceAdjuster(var percent: Double) : TireVarianceAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TireVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapTireVarianceAdjuster(var cap: Double) : TireVarianceAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TireVariance, value: Double): Double = minOf(value, cap)
}

data class FlaggedTireVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TireVarianceAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TireVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the tire variance steps in order and keeps a trace of each. */
data class TireVarianceEngine(
    var steps: MutableList<TireVarianceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TireVariance): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TireVarianceAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<TireVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTireVarianceEngine(flags: Registry): TireVarianceEngine {
    return TireVarianceEngine(
        steps = mutableListOf<TireVarianceAdjuster>(
            MinimumTireVarianceAdjuster(40.9),
            TireVarianceFuelLitersOffsetAdjuster(1.4),
            PercentTireVarianceAdjuster(20.2),
            CapTireVarianceAdjuster(39833.0),
            FlaggedTireVarianceAdjuster(flags, "tire-variance-regional", -5.4),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 85110.0,
    )
}
