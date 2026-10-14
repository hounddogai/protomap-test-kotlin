package com.acme.transport.safety.violation.slot

import com.acme.transport.kit.numbers.roundTo

/** One step of the violation slot calculation. */
interface ViolationSlotModifier {
    fun describe(): String
    fun adjust(item: ViolationSlot, value: Double): Double
}

data class PercentViolationSlotAdjuster(var percent: Double) : ViolationSlotModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ViolationSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredViolationSlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ViolationSlotModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ViolationSlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingViolationSlotAdjuster(var step: Double) : ViolationSlotModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ViolationSlot, value: Double): Double = roundTo(value, step)
}

data class ChannelViolationSlotAdjuster(var factors: MutableMap<String, Double>) : ViolationSlotModifier {
    override fun describe(): String = "by channel"

    override fun adjust(item: ViolationSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the violation slot steps in order and keeps a trace of each. */
data class ViolationSlotEngine(
    var steps: MutableList<ViolationSlotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ViolationSlot): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ViolationSlotModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ViolationSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultViolationSlotEngine(): ViolationSlotEngine {
    return ViolationSlotEngine(
        steps = mutableListOf<ViolationSlotModifier>(
            PercentViolationSlotAdjuster(20.3),
            TieredViolationSlotAdjuster(
                mutableListOf<Double>(2083.0, 4777.0, 5370.0, 5599.0),
                mutableListOf<Double>(1.1, 1.07, 0.98, 0.99),
            ),
            RoundingViolationSlotAdjuster(0.1),
            ChannelViolationSlotAdjuster(mutableMapOf<String, Double>("north" to 1.16, "west" to 1.14)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 44627.3,
    )
}
