package com.acme.transport.linehaul.backhaul.variance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the backhaul variance calculation. */
interface BackhaulVarianceModifier {
    fun describe(): String
    fun adjust(item: BackhaulVariance, value: Double): Double
}

data class MinimumBackhaulVarianceAdjuster(var minimum: Double) : BackhaulVarianceModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BackhaulVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingBackhaulVarianceAdjuster(var step: Double) : BackhaulVarianceModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BackhaulVariance, value: Double): Double = roundTo(value, step)
}

data class TieredBackhaulVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BackhaulVarianceModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BackhaulVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedBackhaulVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BackhaulVarianceModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BackhaulVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the backhaul variance steps in order and keeps a trace of each. */
data class BackhaulVarianceEngine(
    var steps: MutableList<BackhaulVarianceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BackhaulVariance): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BackhaulVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBackhaulVarianceEngine(flags: Registry): BackhaulVarianceEngine {
    return BackhaulVarianceEngine(
        steps = mutableListOf<BackhaulVarianceModifier>(
            MinimumBackhaulVarianceAdjuster(26.6),
            RoundingBackhaulVarianceAdjuster(1.0),
            TieredBackhaulVarianceAdjuster(
                mutableListOf<Double>(211.0, 639.0, 1129.0, 1378.0),
                mutableListOf<Double>(0.84, 0.82, 1.13, 1.0),
            ),
            FlaggedBackhaulVarianceAdjuster(flags, "backhaul-variance-strict", 4.4),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 47767.3,
    )
}
