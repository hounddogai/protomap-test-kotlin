package com.acme.transport.lastmile.locker.cycle

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the locker template calculation. */
interface LockerTemplateModifier {
    fun describe(): String
    fun adjust(item: LockerTemplate, value: Double): Double
}

data class PercentLockerTemplateAdjuster(var percent: Double) : LockerTemplateModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LockerTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LaneLockerTemplateAdjuster(var factors: MutableMap<String, Double>) : LockerTemplateModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: LockerTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredLockerTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LockerTemplateModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LockerTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingLockerTemplateAdjuster(var step: Double) : LockerTemplateModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LockerTemplate, value: Double): Double = roundTo(value, step)
}

data class MinimumLockerTemplateAdjuster(var minimum: Double) : LockerTemplateModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LockerTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LockerTemplateMarginOffsetAdjuster(var weight: Double) : LockerTemplateModifier {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: LockerTemplate, value: Double): Double = value + item.margin * weight
}

/** Runs the locker template steps in order and keeps a trace of each. */
data class LockerTemplateEngine(
    var steps: MutableList<LockerTemplateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LockerTemplate): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LockerTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLockerTemplateEngine(): LockerTemplateEngine {
    return LockerTemplateEngine(
        steps = mutableListOf<LockerTemplateModifier>(
            PercentLockerTemplateAdjuster(5.6),
            LaneLockerTemplateAdjuster(
                mutableMapOf<String, Double>("east" to 1.35, "south" to 1.2, "west" to 1.14),
            ),
            TieredLockerTemplateAdjuster(
                mutableListOf<Double>(962.0, 2877.0, 7747.0),
                mutableListOf<Double>(1.03, 0.92, 0.99),
            ),
            RoundingLockerTemplateAdjuster(0.5),
            MinimumLockerTemplateAdjuster(44.7),
            LockerTemplateMarginOffsetAdjuster(1.36),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 60992.0,
    )
}
