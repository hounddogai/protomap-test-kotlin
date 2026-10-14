package com.acme.transport.lastmile.parcel.tieredparcelvariance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rolling parcel variance calculation. */
interface RollingParcelVarianceStep {
    fun describe(): String
    fun adjust(item: RollingParcelVariance, value: Double): Double
}

data class CapRollingParcelVarianceAdjuster(var cap: Double) : RollingParcelVarianceStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingParcelVariance, value: Double): Double = minOf(value, cap)
}

data class PercentRollingParcelVarianceAdjuster(var percent: Double) : RollingParcelVarianceStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingParcelVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RollingParcelVarianceCreatedAtOffsetAdjuster(var weight: Double) : RollingParcelVarianceStep {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: RollingParcelVariance, value: Double): Double {
        return value + item.createdAt.toDouble() * weight
    }
}

data class MinimumRollingParcelVarianceAdjuster(var minimum: Double) : RollingParcelVarianceStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingParcelVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedRollingParcelVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingParcelVarianceStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingParcelVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredRollingParcelVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingParcelVarianceStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingParcelVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the rolling parcel variance steps in order and keeps a trace of each. */
data class RollingParcelVarianceEngine(
    var steps: MutableList<RollingParcelVarianceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RollingParcelVariance): Double {
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

    fun evaluateAll(items: MutableList<RollingParcelVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRollingParcelVarianceEngine(flags: Registry): RollingParcelVarianceEngine {
    return RollingParcelVarianceEngine(
        steps = mutableListOf<RollingParcelVarianceStep>(
            CapRollingParcelVarianceAdjuster(29556.0),
            PercentRollingParcelVarianceAdjuster(6.2),
            RollingParcelVarianceCreatedAtOffsetAdjuster(0.68),
            MinimumRollingParcelVarianceAdjuster(39.6),
            FlaggedRollingParcelVarianceAdjuster(flags, "rolling-parcel-variance-strict", -3.5),
            TieredRollingParcelVarianceAdjuster(
                mutableListOf<Double>(4671.0, 5180.0, 7549.0, 7714.0),
                mutableListOf<Double>(0.83, 0.93, 1.0, 1.09),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 55763.6,
    )
}
