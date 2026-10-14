package com.acme.transport.terminal.sortation.sortationdigest

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tiered sortation template calculation. */
interface TieredSortationTemplateFactor {
    fun describe(): String
    fun adjust(item: TieredSortationTemplate, value: Double): Double
}

data class SegmentTieredSortationTemplateAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredSortationTemplateFactor {
    override fun describe(): String = "by segment"

    override fun adjust(item: TieredSortationTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumTieredSortationTemplateAdjuster(var minimum: Double) : TieredSortationTemplateFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredSortationTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredTieredSortationTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredSortationTemplateFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredSortationTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the tiered sortation template steps in order and keeps a trace of each. */
data class TieredSortationTemplateEngine(
    var steps: MutableList<TieredSortationTemplateFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TieredSortationTemplate): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredSortationTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTieredSortationTemplateEngine(): TieredSortationTemplateEngine {
    return TieredSortationTemplateEngine(
        steps = mutableListOf<TieredSortationTemplateFactor>(
            SegmentTieredSortationTemplateAdjuster(mutableMapOf<String, Double>("south" to 1.23)),
            MinimumTieredSortationTemplateAdjuster(21.1),
            TieredTieredSortationTemplateAdjuster(
                mutableListOf<Double>(1560.0, 3286.0),
                mutableListOf<Double>(1.16, 1.18),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 58153.9,
    )
}
