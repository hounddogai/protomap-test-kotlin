package com.acme.transport.customs.hold.mapping

/** One step of the hold slot calculation. */
interface HoldSlotAdjuster {
    fun describe(): String
    fun adjust(item: HoldSlot, value: Double): Double
}

data class TieredHoldSlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : HoldSlotAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: HoldSlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class HoldSlotRevisionOffsetAdjuster(var weight: Double) : HoldSlotAdjuster {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: HoldSlot, value: Double): Double = value + item.revision.toDouble() * weight
}

data class PercentHoldSlotAdjuster(var percent: Double) : HoldSlotAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: HoldSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the hold slot steps in order and keeps a trace of each. */
data class HoldSlotEngine(
    var steps: MutableList<HoldSlotAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: HoldSlot): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: HoldSlotAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<HoldSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultHoldSlotEngine(): HoldSlotEngine {
    return HoldSlotEngine(
        steps = mutableListOf<HoldSlotAdjuster>(
            TieredHoldSlotAdjuster(
                mutableListOf<Double>(762.0, 1354.0, 4779.0),
                mutableListOf<Double>(0.82, 1.02, 1.08),
            ),
            HoldSlotRevisionOffsetAdjuster(1.66),
            PercentHoldSlotAdjuster(12.1),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 86001.5,
    )
}
