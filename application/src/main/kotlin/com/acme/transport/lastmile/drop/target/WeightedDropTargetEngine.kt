package com.acme.transport.lastmile.drop.target

import com.acme.transport.kit.flags.Registry

/** One step of the weighted drop target calculation. */
interface WeightedDropTargetModifier {
    fun describe(): String
    fun adjust(item: WeightedDropTarget, value: Double): Double
}

data class FlaggedWeightedDropTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedDropTargetModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedDropTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ChannelWeightedDropTargetAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedDropTargetModifier {
    override fun describe(): String = "by channel"

    override fun adjust(item: WeightedDropTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumWeightedDropTargetAdjuster(var minimum: Double) : WeightedDropTargetModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedDropTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the weighted drop target steps in order and keeps a trace of each. */
data class WeightedDropTargetEngine(
    var steps: MutableList<WeightedDropTargetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: WeightedDropTarget): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedDropTargetModifier = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedDropTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultWeightedDropTargetEngine(flags: Registry): WeightedDropTargetEngine {
    return WeightedDropTargetEngine(
        steps = mutableListOf<WeightedDropTargetModifier>(
            FlaggedWeightedDropTargetAdjuster(flags, "weighted-drop-target-strict", -8.6),
            ChannelWeightedDropTargetAdjuster(mutableMapOf<String, Double>("east" to 0.92, "online" to 1.27)),
            MinimumWeightedDropTargetAdjuster(16.4),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 58540.1,
    )
}
