package com.acme.transport.network.servicearea.areathreshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the default service area rollup calculation. */
interface DefaultServiceAreaRollupModifier {
    fun describe(): String
    fun adjust(item: DefaultServiceAreaRollup, value: Double): Double
}

data class FlaggedDefaultServiceAreaRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultServiceAreaRollupModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultServiceAreaRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredDefaultServiceAreaRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultServiceAreaRollupModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultServiceAreaRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingDefaultServiceAreaRollupAdjuster(var step: Double) : DefaultServiceAreaRollupModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultServiceAreaRollup, value: Double): Double = roundTo(value, step)
}

data class NameDefaultServiceAreaRollupAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultServiceAreaRollupModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: DefaultServiceAreaRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentDefaultServiceAreaRollupAdjuster(var percent: Double) : DefaultServiceAreaRollupModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultServiceAreaRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the default service area rollup steps in order and keeps a trace of each. */
data class DefaultServiceAreaRollupEngine(
    var steps: MutableList<DefaultServiceAreaRollupModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefaultServiceAreaRollup): Double {
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

    fun evaluateAll(items: MutableList<DefaultServiceAreaRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefaultServiceAreaRollupEngine(flags: Registry): DefaultServiceAreaRollupEngine {
    return DefaultServiceAreaRollupEngine(
        steps = mutableListOf<DefaultServiceAreaRollupModifier>(
            FlaggedDefaultServiceAreaRollupAdjuster(flags, "default-service-area-rollup-rolling", 4.3),
            TieredDefaultServiceAreaRollupAdjuster(
                mutableListOf<Double>(1467.0, 6624.0, 9791.0),
                mutableListOf<Double>(1.13, 0.88, 0.9),
            ),
            RoundingDefaultServiceAreaRollupAdjuster(0.05),
            NameDefaultServiceAreaRollupAdjuster(
                mutableMapOf<String, Double>("north" to 0.89, "central" to 1.06, "south" to 0.75),
            ),
            PercentDefaultServiceAreaRollupAdjuster(23.6),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 61014.5,
    )
}
