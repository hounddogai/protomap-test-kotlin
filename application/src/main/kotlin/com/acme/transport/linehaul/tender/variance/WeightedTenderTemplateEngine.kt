package com.acme.transport.linehaul.tender.variance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the weighted tender template calculation. */
interface WeightedTenderTemplateAdjuster {
    fun describe(): String
    fun adjust(item: WeightedTenderTemplate, value: Double): Double
}

data class UnitWeightedTenderTemplateAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedTenderTemplateAdjuster {
    override fun describe(): String = "by unit"

    override fun adjust(item: WeightedTenderTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class WeightedTenderTemplateStopCountOffsetAdjuster(var weight: Double) : WeightedTenderTemplateAdjuster {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: WeightedTenderTemplate, value: Double): Double {
        return value + item.stopCount.toDouble() * weight
    }
}

data class RoundingWeightedTenderTemplateAdjuster(var step: Double) : WeightedTenderTemplateAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedTenderTemplate, value: Double): Double = roundTo(value, step)
}

data class TieredWeightedTenderTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedTenderTemplateAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedTenderTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumWeightedTenderTemplateAdjuster(var minimum: Double) : WeightedTenderTemplateAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedTenderTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedWeightedTenderTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedTenderTemplateAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedTenderTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the weighted tender template steps in order and keeps a trace of each. */
data class WeightedTenderTemplateEngine(
    var steps: MutableList<WeightedTenderTemplateAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: WeightedTenderTemplate): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedTenderTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultWeightedTenderTemplateEngine(flags: Registry): WeightedTenderTemplateEngine {
    return WeightedTenderTemplateEngine(
        steps = mutableListOf<WeightedTenderTemplateAdjuster>(
            UnitWeightedTenderTemplateAdjuster(mutableMapOf<String, Double>("online" to 0.74)),
            WeightedTenderTemplateStopCountOffsetAdjuster(1.91),
            RoundingWeightedTenderTemplateAdjuster(0.5),
            TieredWeightedTenderTemplateAdjuster(
                mutableListOf<Double>(1239.0, 2272.0, 7257.0),
                mutableListOf<Double>(1.17, 1.06, 1.01),
            ),
            MinimumWeightedTenderTemplateAdjuster(19.7),
            FlaggedWeightedTenderTemplateAdjuster(flags, "weighted-tender-template-seasonal", 9.6),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 19937.5,
    )
}
