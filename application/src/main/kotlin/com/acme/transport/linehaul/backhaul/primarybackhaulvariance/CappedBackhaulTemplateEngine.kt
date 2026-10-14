package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped backhaul template calculation. */
interface CappedBackhaulTemplateStep {
    fun describe(): String
    fun adjust(item: CappedBackhaulTemplate, value: Double): Double
}

data class CapCappedBackhaulTemplateAdjuster(var cap: Double) : CappedBackhaulTemplateStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedBackhaulTemplate, value: Double): Double = minOf(value, cap)
}

data class MinimumCappedBackhaulTemplateAdjuster(var minimum: Double) : CappedBackhaulTemplateStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedBackhaulTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingCappedBackhaulTemplateAdjuster(var step: Double) : CappedBackhaulTemplateStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedBackhaulTemplate, value: Double): Double = roundTo(value, step)
}

data class TieredCappedBackhaulTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedBackhaulTemplateStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedBackhaulTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentCappedBackhaulTemplateAdjuster(var percent: Double) : CappedBackhaulTemplateStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedBackhaulTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RegionCappedBackhaulTemplateAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedBackhaulTemplateStep {
    override fun describe(): String = "by region"

    override fun adjust(item: CappedBackhaulTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the capped backhaul template steps in order and keeps a trace of each. */
data class CappedBackhaulTemplateEngine(
    var steps: MutableList<CappedBackhaulTemplateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedBackhaulTemplate): Double {
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

    fun evaluateAll(items: MutableList<CappedBackhaulTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedBackhaulTemplateEngine(): CappedBackhaulTemplateEngine {
    return CappedBackhaulTemplateEngine(
        steps = mutableListOf<CappedBackhaulTemplateStep>(
            CapCappedBackhaulTemplateAdjuster(39139.0),
            MinimumCappedBackhaulTemplateAdjuster(6.3),
            RoundingCappedBackhaulTemplateAdjuster(1.0),
            TieredCappedBackhaulTemplateAdjuster(
                mutableListOf<Double>(2634.0, 2720.0),
                mutableListOf<Double>(1.11, 0.85),
            ),
            PercentCappedBackhaulTemplateAdjuster(-3.5),
            RegionCappedBackhaulTemplateAdjuster(mutableMapOf<String, Double>("retail" to 1.2, "online" to 0.9)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 81732.0,
    )
}
