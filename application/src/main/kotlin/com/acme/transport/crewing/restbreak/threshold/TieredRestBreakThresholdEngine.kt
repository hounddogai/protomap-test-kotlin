package com.acme.transport.crewing.restbreak.threshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered rest break threshold calculation. */
interface TieredRestBreakThresholdModifier {
    fun describe(): String
    fun adjust(item: TieredRestBreakThreshold, value: Double): Double
}

data class TieredTieredRestBreakThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredRestBreakThresholdModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredRestBreakThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingTieredRestBreakThresholdAdjuster(var step: Double) : TieredRestBreakThresholdModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredRestBreakThreshold, value: Double): Double = roundTo(value, step)
}

data class PercentTieredRestBreakThresholdAdjuster(var percent: Double) : TieredRestBreakThresholdModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredRestBreakThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedTieredRestBreakThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredRestBreakThresholdModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredRestBreakThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TitleTieredRestBreakThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredRestBreakThresholdModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: TieredRestBreakThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the tiered rest break threshold steps in order and keeps a trace of each. */
data class TieredRestBreakThresholdEngine(
    var steps: MutableList<TieredRestBreakThresholdModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TieredRestBreakThreshold): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredRestBreakThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTieredRestBreakThresholdEngine(flags: Registry): TieredRestBreakThresholdEngine {
    return TieredRestBreakThresholdEngine(
        steps = mutableListOf<TieredRestBreakThresholdModifier>(
            TieredTieredRestBreakThresholdAdjuster(
                mutableListOf<Double>(3026.0, 6009.0, 9419.0),
                mutableListOf<Double>(1.02, 0.94, 0.86),
            ),
            RoundingTieredRestBreakThresholdAdjuster(1.0),
            PercentTieredRestBreakThresholdAdjuster(24.1),
            FlaggedTieredRestBreakThresholdAdjuster(flags, "tiered-rest-break-threshold-batched", -5.9),
            TitleTieredRestBreakThresholdAdjuster(
                mutableMapOf<String, Double>("online" to 0.86, "west" to 1.21),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 88477.7,
    )
}
