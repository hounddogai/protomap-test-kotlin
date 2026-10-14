package com.acme.transport.customs.hold.variance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered hold variance calculation. */
interface TieredHoldVarianceAdjuster {
    fun describe(): String
    fun adjust(item: TieredHoldVariance, value: Double): Double
}

data class MinimumTieredHoldVarianceAdjuster(var minimum: Double) : TieredHoldVarianceAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredHoldVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedTieredHoldVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredHoldVarianceAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredHoldVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredHoldVarianceRetriesOffsetAdjuster(var weight: Double) : TieredHoldVarianceAdjuster {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: TieredHoldVariance, value: Double): Double = value + item.retries.toDouble() * weight
}

data class RoundingTieredHoldVarianceAdjuster(var step: Double) : TieredHoldVarianceAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredHoldVariance, value: Double): Double = roundTo(value, step)
}

data class PercentTieredHoldVarianceAdjuster(var percent: Double) : TieredHoldVarianceAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredHoldVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the tiered hold variance steps in order and keeps a trace of each. */
data class TieredHoldVarianceEngine(
    var steps: MutableList<TieredHoldVarianceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TieredHoldVariance): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredHoldVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTieredHoldVarianceEngine(flags: Registry): TieredHoldVarianceEngine {
    return TieredHoldVarianceEngine(
        steps = mutableListOf<TieredHoldVarianceAdjuster>(
            MinimumTieredHoldVarianceAdjuster(28.7),
            FlaggedTieredHoldVarianceAdjuster(flags, "tiered-hold-variance-global", 5.0),
            TieredHoldVarianceRetriesOffsetAdjuster(0.38),
            RoundingTieredHoldVarianceAdjuster(5.0),
            PercentTieredHoldVarianceAdjuster(17.7),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 27627.1,
    )
}
