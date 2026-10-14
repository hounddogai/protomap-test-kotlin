package com.acme.transport.lastmile.redelivery.slot

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the manual redelivery window calculation. */
interface ManualRedeliveryWindowModifier {
    fun describe(): String
    fun adjust(item: ManualRedeliveryWindow, value: Double): Double
}

data class RoundingManualRedeliveryWindowAdjuster(var step: Double) : ManualRedeliveryWindowModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualRedeliveryWindow, value: Double): Double = roundTo(value, step)
}

data class TieredManualRedeliveryWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualRedeliveryWindowModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualRedeliveryWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentManualRedeliveryWindowAdjuster(var percent: Double) : ManualRedeliveryWindowModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualRedeliveryWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapManualRedeliveryWindowAdjuster(var cap: Double) : ManualRedeliveryWindowModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualRedeliveryWindow, value: Double): Double = minOf(value, cap)
}

data class ManualRedeliveryWindowCapacityOffsetAdjuster(var weight: Double) : ManualRedeliveryWindowModifier {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: ManualRedeliveryWindow, value: Double): Double {
        return value + item.capacity.toDouble() * weight
    }
}

data class CategoryManualRedeliveryWindowAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualRedeliveryWindowModifier {
    override fun describe(): String = "by category"

    override fun adjust(item: ManualRedeliveryWindow, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the manual redelivery window steps in order and keeps a trace of each. */
data class ManualRedeliveryWindowEngine(
    var steps: MutableList<ManualRedeliveryWindowModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualRedeliveryWindow): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualRedeliveryWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualRedeliveryWindowEngine(): ManualRedeliveryWindowEngine {
    return ManualRedeliveryWindowEngine(
        steps = mutableListOf<ManualRedeliveryWindowModifier>(
            RoundingManualRedeliveryWindowAdjuster(0.25),
            TieredManualRedeliveryWindowAdjuster(
                mutableListOf<Double>(1220.0, 8935.0),
                mutableListOf<Double>(0.99, 0.82),
            ),
            PercentManualRedeliveryWindowAdjuster(-13.1),
            CapManualRedeliveryWindowAdjuster(12398.0),
            ManualRedeliveryWindowCapacityOffsetAdjuster(1.55),
            CategoryManualRedeliveryWindowAdjuster(
                mutableMapOf<String, Double>("retail" to 1.13, "south" to 0.92, "east" to 0.87),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 99028.6,
    )
}
