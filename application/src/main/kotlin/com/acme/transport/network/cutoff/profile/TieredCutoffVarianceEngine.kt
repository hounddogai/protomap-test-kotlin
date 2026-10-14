package com.acme.transport.network.cutoff.profile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered cutoff variance calculation. */
interface TieredCutoffVarianceFactor {
    fun describe(): String
    fun adjust(item: TieredCutoffVariance, value: Double): Double
}

data class SegmentTieredCutoffVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredCutoffVarianceFactor {
    override fun describe(): String = "by segment"

    override fun adjust(item: TieredCutoffVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedTieredCutoffVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredCutoffVarianceFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredCutoffVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentTieredCutoffVarianceAdjuster(var percent: Double) : TieredCutoffVarianceFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredCutoffVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredCutoffVarianceScoreOffsetAdjuster(var weight: Double) : TieredCutoffVarianceFactor {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: TieredCutoffVariance, value: Double): Double = value + item.score * weight
}

data class RoundingTieredCutoffVarianceAdjuster(var step: Double) : TieredCutoffVarianceFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredCutoffVariance, value: Double): Double = roundTo(value, step)
}

/** Runs the tiered cutoff variance steps in order and keeps a trace of each. */
data class TieredCutoffVarianceEngine(
    var steps: MutableList<TieredCutoffVarianceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TieredCutoffVariance): Double {
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

    fun evaluateAll(items: MutableList<TieredCutoffVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTieredCutoffVarianceEngine(flags: Registry): TieredCutoffVarianceEngine {
    return TieredCutoffVarianceEngine(
        steps = mutableListOf<TieredCutoffVarianceFactor>(
            SegmentTieredCutoffVarianceAdjuster(mutableMapOf<String, Double>("central" to 1.23, "south" to 1.1)),
            FlaggedTieredCutoffVarianceAdjuster(flags, "tiered-cutoff-variance-legacy", 2.1),
            PercentTieredCutoffVarianceAdjuster(19.2),
            TieredCutoffVarianceScoreOffsetAdjuster(1.91),
            RoundingTieredCutoffVarianceAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 80688.5,
    )
}
