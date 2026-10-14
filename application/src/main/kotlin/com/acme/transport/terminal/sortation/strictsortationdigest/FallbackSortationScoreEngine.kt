package com.acme.transport.terminal.sortation.strictsortationdigest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback sortation score calculation. */
interface FallbackSortationScoreFactor {
    fun describe(): String
    fun adjust(item: FallbackSortationScore, value: Double): Double
}

data class RoundingFallbackSortationScoreAdjuster(var step: Double) : FallbackSortationScoreFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackSortationScore, value: Double): Double = roundTo(value, step)
}

data class LabelFallbackSortationScoreAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackSortationScoreFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: FallbackSortationScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedFallbackSortationScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackSortationScoreFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackSortationScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredFallbackSortationScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackSortationScoreFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackSortationScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the fallback sortation score steps in order and keeps a trace of each. */
data class FallbackSortationScoreEngine(
    var steps: MutableList<FallbackSortationScoreFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FallbackSortationScore): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackSortationScoreFactor = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackSortationScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFallbackSortationScoreEngine(flags: Registry): FallbackSortationScoreEngine {
    return FallbackSortationScoreEngine(
        steps = mutableListOf<FallbackSortationScoreFactor>(
            RoundingFallbackSortationScoreAdjuster(0.1),
            LabelFallbackSortationScoreAdjuster(mutableMapOf<String, Double>("east" to 1.08)),
            FlaggedFallbackSortationScoreAdjuster(flags, "fallback-sortation-score-primary", 4.6),
            TieredFallbackSortationScoreAdjuster(
                mutableListOf<Double>(729.0, 6077.0, 9987.0),
                mutableListOf<Double>(0.94, 1.18, 1.13),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 88939.0,
    )
}
