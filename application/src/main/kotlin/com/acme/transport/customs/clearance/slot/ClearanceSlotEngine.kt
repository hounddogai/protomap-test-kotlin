package com.acme.transport.customs.clearance.slot

/** One step of the clearance slot calculation. */
interface ClearanceSlotModifier {
    fun describe(): String
    fun adjust(item: ClearanceSlot, value: Double): Double
}

data class PercentClearanceSlotAdjuster(var percent: Double) : ClearanceSlotModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ClearanceSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumClearanceSlotAdjuster(var minimum: Double) : ClearanceSlotModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ClearanceSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ClearanceSlotRateOffsetAdjuster(var weight: Double) : ClearanceSlotModifier {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: ClearanceSlot, value: Double): Double = value + item.rate * weight
}

/** Runs the clearance slot steps in order and keeps a trace of each. */
data class ClearanceSlotEngine(
    var steps: MutableList<ClearanceSlotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ClearanceSlot): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ClearanceSlotModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ClearanceSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultClearanceSlotEngine(): ClearanceSlotEngine {
    return ClearanceSlotEngine(
        steps = mutableListOf<ClearanceSlotModifier>(
            PercentClearanceSlotAdjuster(-9.9),
            MinimumClearanceSlotAdjuster(15.6),
            ClearanceSlotRateOffsetAdjuster(0.28),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 20231.3,
    )
}
