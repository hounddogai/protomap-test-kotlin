package com.acme.transport.safety.scorecard.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the scorecard variance calculation. */
interface ScorecardVarianceAdjuster {
    fun describe(): String
    fun adjust(item: ScorecardVariance, value: Double): Double
}

data class TieredScorecardVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScorecardVarianceAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScorecardVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingScorecardVarianceAdjuster(var step: Double) : ScorecardVarianceAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScorecardVariance, value: Double): Double = roundTo(value, step)
}

data class FlaggedScorecardVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScorecardVarianceAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScorecardVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class SourceScorecardVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : ScorecardVarianceAdjuster {
    override fun describe(): String = "by source"

    override fun adjust(item: ScorecardVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the scorecard variance steps in order and keeps a trace of each. */
data class ScorecardVarianceEngine(
    var steps: MutableList<ScorecardVarianceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ScorecardVariance): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScorecardVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultScorecardVarianceEngine(flags: Registry): ScorecardVarianceEngine {
    return ScorecardVarianceEngine(
        steps = mutableListOf<ScorecardVarianceAdjuster>(
            TieredScorecardVarianceAdjuster(
                mutableListOf<Double>(1750.0, 6772.0, 7580.0, 7912.0),
                mutableListOf<Double>(0.83, 1.04, 1.19, 1.02),
            ),
            RoundingScorecardVarianceAdjuster(0.25),
            FlaggedScorecardVarianceAdjuster(flags, "scorecard-variance-regional", -9.0),
            SourceScorecardVarianceAdjuster(mutableMapOf<String, Double>("online" to 0.94)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 5538.4,
    )
}
