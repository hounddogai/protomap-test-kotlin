package com.acme.transport.terminal.crossdock.strictdockslot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the weighted cross dock slot calculation. */
interface WeightedCrossDockSlotAdjuster {
    fun describe(): String
    fun adjust(item: WeightedCrossDockSlot, value: Double): Double
}

data class FlaggedWeightedCrossDockSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedCrossDockSlotAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedCrossDockSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentWeightedCrossDockSlotAdjuster(var percent: Double) : WeightedCrossDockSlotAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedCrossDockSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingWeightedCrossDockSlotAdjuster(var step: Double) : WeightedCrossDockSlotAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedCrossDockSlot, value: Double): Double = roundTo(value, step)
}

/** Runs the weighted cross dock slot steps in order and keeps a trace of each. */
data class WeightedCrossDockSlotEngine(
    var steps: MutableList<WeightedCrossDockSlotAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: WeightedCrossDockSlot): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedCrossDockSlotAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedCrossDockSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultWeightedCrossDockSlotEngine(flags: Registry): WeightedCrossDockSlotEngine {
    return WeightedCrossDockSlotEngine(
        steps = mutableListOf<WeightedCrossDockSlotAdjuster>(
            FlaggedWeightedCrossDockSlotAdjuster(flags, "weighted-cross-dock-slot-tiered", 3.3),
            PercentWeightedCrossDockSlotAdjuster(-8.3),
            RoundingWeightedCrossDockSlotAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 87628.0,
    )
}
