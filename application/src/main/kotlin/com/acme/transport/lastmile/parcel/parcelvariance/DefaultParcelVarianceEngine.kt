package com.acme.transport.lastmile.parcel.parcelvariance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the default parcel variance calculation. */
interface DefaultParcelVarianceAdjuster {
    fun describe(): String
    fun adjust(item: DefaultParcelVariance, value: Double): Double
}

data class MinimumDefaultParcelVarianceAdjuster(var minimum: Double) : DefaultParcelVarianceAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultParcelVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingDefaultParcelVarianceAdjuster(var step: Double) : DefaultParcelVarianceAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultParcelVariance, value: Double): Double = roundTo(value, step)
}

data class FlaggedDefaultParcelVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultParcelVarianceAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultParcelVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentDefaultParcelVarianceAdjuster(var percent: Double) : DefaultParcelVarianceAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultParcelVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class DefaultParcelVarianceDistanceKmOffsetAdjuster(var weight: Double) : DefaultParcelVarianceAdjuster {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: DefaultParcelVariance, value: Double): Double = value + item.distanceKm * weight
}

data class CapDefaultParcelVarianceAdjuster(var cap: Double) : DefaultParcelVarianceAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultParcelVariance, value: Double): Double = minOf(value, cap)
}

/** Runs the default parcel variance steps in order and keeps a trace of each. */
data class DefaultParcelVarianceEngine(
    var steps: MutableList<DefaultParcelVarianceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DefaultParcelVariance): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultParcelVarianceAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultParcelVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDefaultParcelVarianceEngine(flags: Registry): DefaultParcelVarianceEngine {
    return DefaultParcelVarianceEngine(
        steps = mutableListOf<DefaultParcelVarianceAdjuster>(
            MinimumDefaultParcelVarianceAdjuster(43.1),
            RoundingDefaultParcelVarianceAdjuster(0.25),
            FlaggedDefaultParcelVarianceAdjuster(flags, "default-parcel-variance-strict", -6.3),
            PercentDefaultParcelVarianceAdjuster(5.7),
            DefaultParcelVarianceDistanceKmOffsetAdjuster(0.38),
            CapDefaultParcelVarianceAdjuster(45123.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 18249.7,
    )
}
