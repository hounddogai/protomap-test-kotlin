package com.acme.transport.network.transittime.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the transit time plan calculation. */
interface TransitTimePlanModifier {
    fun describe(): String
    fun adjust(item: TransitTimePlan, value: Double): Double
}

data class MinimumTransitTimePlanAdjuster(var minimum: Double) : TransitTimePlanModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TransitTimePlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TitleTransitTimePlanAdjuster(var factors: MutableMap<String, Double>) : TransitTimePlanModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: TransitTimePlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedTransitTimePlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TransitTimePlanModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TransitTimePlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingTransitTimePlanAdjuster(var step: Double) : TransitTimePlanModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TransitTimePlan, value: Double): Double = roundTo(value, step)
}

data class CapTransitTimePlanAdjuster(var cap: Double) : TransitTimePlanModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TransitTimePlan, value: Double): Double = minOf(value, cap)
}

data class TieredTransitTimePlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TransitTimePlanModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TransitTimePlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the transit time plan steps in order and keeps a trace of each. */
data class TransitTimePlanEngine(
    var steps: MutableList<TransitTimePlanModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TransitTimePlan): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TransitTimePlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTransitTimePlanEngine(flags: Registry): TransitTimePlanEngine {
    return TransitTimePlanEngine(
        steps = mutableListOf<TransitTimePlanModifier>(
            MinimumTransitTimePlanAdjuster(46.2),
            TitleTransitTimePlanAdjuster(mutableMapOf<String, Double>("online" to 1.2, "west" to 1.29)),
            FlaggedTransitTimePlanAdjuster(flags, "transit-time-plan-manual", 6.7),
            RoundingTransitTimePlanAdjuster(0.05),
            CapTransitTimePlanAdjuster(8776.0),
            TieredTransitTimePlanAdjuster(
                mutableListOf<Double>(1964.0, 8984.0),
                mutableListOf<Double>(0.8, 1.18),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 50343.0,
    )
}
