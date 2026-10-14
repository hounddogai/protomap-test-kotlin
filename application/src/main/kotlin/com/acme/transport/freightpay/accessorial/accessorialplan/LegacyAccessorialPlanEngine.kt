package com.acme.transport.freightpay.accessorial.accessorialplan

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the legacy accessorial plan calculation. */
interface LegacyAccessorialPlanModifier {
    fun describe(): String
    fun adjust(item: LegacyAccessorialPlan, value: Double): Double
}

data class MinimumLegacyAccessorialPlanAdjuster(var minimum: Double) : LegacyAccessorialPlanModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyAccessorialPlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NameLegacyAccessorialPlanAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyAccessorialPlanModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: LegacyAccessorialPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingLegacyAccessorialPlanAdjuster(var step: Double) : LegacyAccessorialPlanModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyAccessorialPlan, value: Double): Double = roundTo(value, step)
}

data class CapLegacyAccessorialPlanAdjuster(var cap: Double) : LegacyAccessorialPlanModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyAccessorialPlan, value: Double): Double = minOf(value, cap)
}

data class TieredLegacyAccessorialPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyAccessorialPlanModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyAccessorialPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the legacy accessorial plan steps in order and keeps a trace of each. */
data class LegacyAccessorialPlanEngine(
    var steps: MutableList<LegacyAccessorialPlanModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LegacyAccessorialPlan): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyAccessorialPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLegacyAccessorialPlanEngine(): LegacyAccessorialPlanEngine {
    return LegacyAccessorialPlanEngine(
        steps = mutableListOf<LegacyAccessorialPlanModifier>(
            MinimumLegacyAccessorialPlanAdjuster(16.5),
            NameLegacyAccessorialPlanAdjuster(mutableMapOf<String, Double>("west" to 0.85)),
            RoundingLegacyAccessorialPlanAdjuster(0.25),
            CapLegacyAccessorialPlanAdjuster(31748.0),
            TieredLegacyAccessorialPlanAdjuster(
                mutableListOf<Double>(5232.0, 7196.0),
                mutableListOf<Double>(1.05, 0.87),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 97396.2,
    )
}
