package com.acme.transport.terminal.dockdoor.threshold

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the dock door threshold calculation. */
interface DockDoorThresholdAdjuster {
    fun describe(): String
    fun adjust(item: DockDoorThreshold, value: Double): Double
}

data class PercentDockDoorThresholdAdjuster(var percent: Double) : DockDoorThresholdAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DockDoorThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class DockDoorThresholdRatioOffsetAdjuster(var weight: Double) : DockDoorThresholdAdjuster {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: DockDoorThreshold, value: Double): Double = value + item.ratio * weight
}

data class MinimumDockDoorThresholdAdjuster(var minimum: Double) : DockDoorThresholdAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DockDoorThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ChannelDockDoorThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : DockDoorThresholdAdjuster {
    override fun describe(): String = "by channel"

    override fun adjust(item: DockDoorThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingDockDoorThresholdAdjuster(var step: Double) : DockDoorThresholdAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DockDoorThreshold, value: Double): Double = roundTo(value, step)
}

/** Runs the dock door threshold steps in order and keeps a trace of each. */
data class DockDoorThresholdEngine(
    var steps: MutableList<DockDoorThresholdAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DockDoorThreshold): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DockDoorThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDockDoorThresholdEngine(): DockDoorThresholdEngine {
    return DockDoorThresholdEngine(
        steps = mutableListOf<DockDoorThresholdAdjuster>(
            PercentDockDoorThresholdAdjuster(-18.8),
            DockDoorThresholdRatioOffsetAdjuster(1.76),
            MinimumDockDoorThresholdAdjuster(22.9),
            ChannelDockDoorThresholdAdjuster(mutableMapOf<String, Double>("north" to 1.17, "west" to 0.87)),
            RoundingDockDoorThresholdAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 64431.8,
    )
}
