package com.acme.transport.network.transittime.batch

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the transit time policy calculation. */
interface TransitTimePolicyStep {
    fun describe(): String
    fun adjust(item: TransitTimePolicy, value: Double): Double
}

data class TransitTimePolicyStopCountOffsetAdjuster(var weight: Double) : TransitTimePolicyStep {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: TransitTimePolicy, value: Double): Double = value + item.stopCount.toDouble() * weight
}

data class TieredTransitTimePolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TransitTimePolicyStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TransitTimePolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TitleTransitTimePolicyAdjuster(var factors: MutableMap<String, Double>) : TransitTimePolicyStep {
    override fun describe(): String = "by title"

    override fun adjust(item: TransitTimePolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumTransitTimePolicyAdjuster(var minimum: Double) : TransitTimePolicyStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TransitTimePolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapTransitTimePolicyAdjuster(var cap: Double) : TransitTimePolicyStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TransitTimePolicy, value: Double): Double = minOf(value, cap)
}

/** Runs the transit time policy steps in order and keeps a trace of each. */
data class TransitTimePolicyEngine(
    var steps: MutableList<TransitTimePolicyStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TransitTimePolicy): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TransitTimePolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTransitTimePolicyEngine(): TransitTimePolicyEngine {
    return TransitTimePolicyEngine(
        steps = mutableListOf<TransitTimePolicyStep>(
            TransitTimePolicyStopCountOffsetAdjuster(0.75),
            TieredTransitTimePolicyAdjuster(
                mutableListOf<Double>(125.0, 2104.0, 6954.0),
                mutableListOf<Double>(0.83, 1.17, 1.14),
            ),
            TitleTransitTimePolicyAdjuster(mutableMapOf<String, Double>("south" to 0.99)),
            MinimumTransitTimePolicyAdjuster(34.0),
            CapTransitTimePolicyAdjuster(40222.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 55003.8,
    )
}
