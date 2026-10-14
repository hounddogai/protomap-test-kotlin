package com.acme.transport.crewing.restbreak.overrideitem

import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy rest break mapping calculation. */
interface LegacyRestBreakMappingAdjuster {
    fun describe(): String
    fun adjust(item: LegacyRestBreakMapping, value: Double): Double
}

data class TieredLegacyRestBreakMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyRestBreakMappingAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyRestBreakMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TitleLegacyRestBreakMappingAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyRestBreakMappingAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: LegacyRestBreakMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingLegacyRestBreakMappingAdjuster(var step: Double) : LegacyRestBreakMappingAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyRestBreakMapping, value: Double): Double = roundTo(value, step)
}

/** Runs the legacy rest break mapping steps in order and keeps a trace of each. */
data class LegacyRestBreakMappingEngine(
    var steps: MutableList<LegacyRestBreakMappingAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyRestBreakMapping): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyRestBreakMappingAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyRestBreakMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyRestBreakMappingEngine(): LegacyRestBreakMappingEngine {
    return LegacyRestBreakMappingEngine(
        steps = mutableListOf<LegacyRestBreakMappingAdjuster>(
            TieredLegacyRestBreakMappingAdjuster(
                mutableListOf<Double>(3861.0, 8762.0),
                mutableListOf<Double>(0.88, 0.98),
            ),
            TitleLegacyRestBreakMappingAdjuster(mutableMapOf<String, Double>("west" to 0.83, "central" to 1.09)),
            RoundingLegacyRestBreakMappingAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 76146.0,
    )
}
