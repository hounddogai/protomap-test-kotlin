package com.acme.transport.safety.nearmiss.variance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the near miss variance calculation. */
interface NearMissVarianceStep {
    fun describe(): String
    fun adjust(item: NearMissVariance, value: Double): Double
}

data class MinimumNearMissVarianceAdjuster(var minimum: Double) : NearMissVarianceStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: NearMissVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentNearMissVarianceAdjuster(var percent: Double) : NearMissVarianceStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: NearMissVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingNearMissVarianceAdjuster(var step: Double) : NearMissVarianceStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: NearMissVariance, value: Double): Double = roundTo(value, step)
}

data class FlaggedNearMissVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : NearMissVarianceStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: NearMissVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredNearMissVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : NearMissVarianceStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: NearMissVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SegmentNearMissVarianceAdjuster(var factors: MutableMap<String, Double>) : NearMissVarianceStep {
    override fun describe(): String = "by segment"

    override fun adjust(item: NearMissVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the near miss variance steps in order and keeps a trace of each. */
data class NearMissVarianceEngine(
    var steps: MutableList<NearMissVarianceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: NearMissVariance): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<NearMissVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultNearMissVarianceEngine(flags: Registry): NearMissVarianceEngine {
    return NearMissVarianceEngine(
        steps = mutableListOf<NearMissVarianceStep>(
            MinimumNearMissVarianceAdjuster(48.2),
            PercentNearMissVarianceAdjuster(12.3),
            RoundingNearMissVarianceAdjuster(0.05),
            FlaggedNearMissVarianceAdjuster(flags, "near-miss-variance-legacy", 7.4),
            TieredNearMissVarianceAdjuster(
                mutableListOf<Double>(269.0, 586.0, 7520.0, 7938.0),
                mutableListOf<Double>(1.08, 1.13, 1.09, 1.07),
            ),
            SegmentNearMissVarianceAdjuster(mutableMapOf<String, Double>("south" to 1.03)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 91438.6,
    )
}
