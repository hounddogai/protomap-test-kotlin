package com.acme.transport.safety.scorecard.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive scorecard checkpoint calculation. */
interface AdaptiveScorecardCheckpointStep {
    fun describe(): String
    fun adjust(item: AdaptiveScorecardCheckpoint, value: Double): Double
}

data class RoundingAdaptiveScorecardCheckpointAdjuster(var step: Double) : AdaptiveScorecardCheckpointStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveScorecardCheckpoint, value: Double): Double = roundTo(value, step)
}

data class PercentAdaptiveScorecardCheckpointAdjuster(var percent: Double) : AdaptiveScorecardCheckpointStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveScorecardCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedAdaptiveScorecardCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveScorecardCheckpointStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveScorecardCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CategoryAdaptiveScorecardCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveScorecardCheckpointStep {
    override fun describe(): String = "by category"

    override fun adjust(item: AdaptiveScorecardCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredAdaptiveScorecardCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveScorecardCheckpointStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveScorecardCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the adaptive scorecard checkpoint steps in order and keeps a trace of each. */
data class AdaptiveScorecardCheckpointEngine(
    var steps: MutableList<AdaptiveScorecardCheckpointStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveScorecardCheckpoint): Double {
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

    fun evaluateAll(items: MutableList<AdaptiveScorecardCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveScorecardCheckpointEngine(flags: Registry): AdaptiveScorecardCheckpointEngine {
    return AdaptiveScorecardCheckpointEngine(
        steps = mutableListOf<AdaptiveScorecardCheckpointStep>(
            RoundingAdaptiveScorecardCheckpointAdjuster(0.1),
            PercentAdaptiveScorecardCheckpointAdjuster(2.1),
            FlaggedAdaptiveScorecardCheckpointAdjuster(flags, "adaptive-scorecard-checkpoint-strict", 2.0),
            CategoryAdaptiveScorecardCheckpointAdjuster(
                mutableMapOf<String, Double>("north" to 0.92, "central" to 0.91),
            ),
            TieredAdaptiveScorecardCheckpointAdjuster(
                mutableListOf<Double>(1211.0, 3995.0, 4523.0, 9829.0),
                mutableListOf<Double>(0.88, 0.8, 1.04, 1.17),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 97117.1,
    )
}
