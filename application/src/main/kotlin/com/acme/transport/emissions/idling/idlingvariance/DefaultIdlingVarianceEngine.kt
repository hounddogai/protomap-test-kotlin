package com.acme.transport.emissions.idling.idlingvariance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the default idling variance calculation. */
interface DefaultIdlingVarianceStep {
    fun describe(): String
    fun adjust(item: DefaultIdlingVariance, value: Double): Double
}

data class DefaultIdlingVarianceBatchSizeOffsetAdjuster(var weight: Double) : DefaultIdlingVarianceStep {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: DefaultIdlingVariance, value: Double): Double {
        return value + item.batchSize.toDouble() * weight
    }
}

data class MinimumDefaultIdlingVarianceAdjuster(var minimum: Double) : DefaultIdlingVarianceStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultIdlingVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentDefaultIdlingVarianceAdjuster(var percent: Double) : DefaultIdlingVarianceStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultIdlingVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredDefaultIdlingVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultIdlingVarianceStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultIdlingVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingDefaultIdlingVarianceAdjuster(var step: Double) : DefaultIdlingVarianceStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultIdlingVariance, value: Double): Double = roundTo(value, step)
}

data class FlaggedDefaultIdlingVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultIdlingVarianceStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultIdlingVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the default idling variance steps in order and keeps a trace of each. */
data class DefaultIdlingVarianceEngine(
    var steps: MutableList<DefaultIdlingVarianceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefaultIdlingVariance): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultIdlingVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefaultIdlingVarianceEngine(flags: Registry): DefaultIdlingVarianceEngine {
    return DefaultIdlingVarianceEngine(
        steps = mutableListOf<DefaultIdlingVarianceStep>(
            DefaultIdlingVarianceBatchSizeOffsetAdjuster(1.06),
            MinimumDefaultIdlingVarianceAdjuster(0.9),
            PercentDefaultIdlingVarianceAdjuster(16.8),
            TieredDefaultIdlingVarianceAdjuster(
                mutableListOf<Double>(8426.0, 8572.0),
                mutableListOf<Double>(1.01, 0.97),
            ),
            RoundingDefaultIdlingVarianceAdjuster(0.5),
            FlaggedDefaultIdlingVarianceAdjuster(flags, "default-idling-variance-weighted", 2.8),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 45532.7,
    )
}
