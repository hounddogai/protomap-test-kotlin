package com.acme.transport.linehaul.capacity.slot

import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback capacity slot calculation. */
interface FallbackCapacitySlotModifier {
    fun describe(): String
    fun adjust(item: FallbackCapacitySlot, value: Double): Double
}

data class CapFallbackCapacitySlotAdjuster(var cap: Double) : FallbackCapacitySlotModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackCapacitySlot, value: Double): Double = minOf(value, cap)
}

data class RoundingFallbackCapacitySlotAdjuster(var step: Double) : FallbackCapacitySlotModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackCapacitySlot, value: Double): Double = roundTo(value, step)
}

data class MinimumFallbackCapacitySlotAdjuster(var minimum: Double) : FallbackCapacitySlotModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackCapacitySlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredFallbackCapacitySlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackCapacitySlotModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackCapacitySlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CategoryFallbackCapacitySlotAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackCapacitySlotModifier {
    override fun describe(): String = "by category"

    override fun adjust(item: FallbackCapacitySlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the fallback capacity slot steps in order and keeps a trace of each. */
data class FallbackCapacitySlotEngine(
    var steps: MutableList<FallbackCapacitySlotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FallbackCapacitySlot): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackCapacitySlotModifier = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackCapacitySlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFallbackCapacitySlotEngine(): FallbackCapacitySlotEngine {
    return FallbackCapacitySlotEngine(
        steps = mutableListOf<FallbackCapacitySlotModifier>(
            CapFallbackCapacitySlotAdjuster(19452.0),
            RoundingFallbackCapacitySlotAdjuster(1.0),
            MinimumFallbackCapacitySlotAdjuster(15.2),
            TieredFallbackCapacitySlotAdjuster(
                mutableListOf<Double>(958.0, 7377.0),
                mutableListOf<Double>(0.81, 0.98),
            ),
            CategoryFallbackCapacitySlotAdjuster(mutableMapOf<String, Double>("online" to 1.27)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 14722.2,
    )
}
