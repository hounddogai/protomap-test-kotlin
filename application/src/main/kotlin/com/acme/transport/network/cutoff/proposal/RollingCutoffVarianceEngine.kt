package com.acme.transport.network.cutoff.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling cutoff variance calculation. */
interface RollingCutoffVarianceFactor {
    fun describe(): String
    fun adjust(item: RollingCutoffVariance, value: Double): Double
}

data class RoundingRollingCutoffVarianceAdjuster(var step: Double) : RollingCutoffVarianceFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingCutoffVariance, value: Double): Double = roundTo(value, step)
}

data class NameRollingCutoffVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingCutoffVarianceFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: RollingCutoffVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumRollingCutoffVarianceAdjuster(var minimum: Double) : RollingCutoffVarianceFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingCutoffVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapRollingCutoffVarianceAdjuster(var cap: Double) : RollingCutoffVarianceFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingCutoffVariance, value: Double): Double = minOf(value, cap)
}

data class TieredRollingCutoffVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingCutoffVarianceFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingCutoffVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedRollingCutoffVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingCutoffVarianceFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingCutoffVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the rolling cutoff variance steps in order and keeps a trace of each. */
data class RollingCutoffVarianceEngine(
    var steps: MutableList<RollingCutoffVarianceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingCutoffVariance): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingCutoffVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingCutoffVarianceEngine(flags: Registry): RollingCutoffVarianceEngine {
    return RollingCutoffVarianceEngine(
        steps = mutableListOf<RollingCutoffVarianceFactor>(
            RoundingRollingCutoffVarianceAdjuster(0.5),
            NameRollingCutoffVarianceAdjuster(
                mutableMapOf<String, Double>("online" to 0.83, "retail" to 1.37, "central" to 1.0),
            ),
            MinimumRollingCutoffVarianceAdjuster(20.0),
            CapRollingCutoffVarianceAdjuster(39976.0),
            TieredRollingCutoffVarianceAdjuster(
                mutableListOf<Double>(1166.0, 5978.0, 6399.0),
                mutableListOf<Double>(0.9, 1.17, 0.97),
            ),
            FlaggedRollingCutoffVarianceAdjuster(flags, "rolling-cutoff-variance-weighted", 2.4),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 57790.0,
    )
}
