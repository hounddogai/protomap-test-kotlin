package com.acme.transport.terminal.dockdoor.assignment

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the default dock door segment calculation. */
interface DefaultDockDoorSegmentStep {
    fun describe(): String
    fun adjust(item: DefaultDockDoorSegment, value: Double): Double
}

data class DefaultDockDoorSegmentThresholdOffsetAdjuster(var weight: Double) : DefaultDockDoorSegmentStep {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: DefaultDockDoorSegment, value: Double): Double = value + item.threshold * weight
}

data class RoundingDefaultDockDoorSegmentAdjuster(var step: Double) : DefaultDockDoorSegmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultDockDoorSegment, value: Double): Double = roundTo(value, step)
}

data class ChannelDefaultDockDoorSegmentAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultDockDoorSegmentStep {
    override fun describe(): String = "by channel"

    override fun adjust(item: DefaultDockDoorSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapDefaultDockDoorSegmentAdjuster(var cap: Double) : DefaultDockDoorSegmentStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultDockDoorSegment, value: Double): Double = minOf(value, cap)
}

data class TieredDefaultDockDoorSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultDockDoorSegmentStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultDockDoorSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumDefaultDockDoorSegmentAdjuster(var minimum: Double) : DefaultDockDoorSegmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultDockDoorSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the default dock door segment steps in order and keeps a trace of each. */
data class DefaultDockDoorSegmentEngine(
    var steps: MutableList<DefaultDockDoorSegmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefaultDockDoorSegment): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultDockDoorSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefaultDockDoorSegmentEngine(): DefaultDockDoorSegmentEngine {
    return DefaultDockDoorSegmentEngine(
        steps = mutableListOf<DefaultDockDoorSegmentStep>(
            DefaultDockDoorSegmentThresholdOffsetAdjuster(1.56),
            RoundingDefaultDockDoorSegmentAdjuster(1.0),
            ChannelDefaultDockDoorSegmentAdjuster(
                mutableMapOf<String, Double>("north" to 1.19, "central" to 1.05),
            ),
            CapDefaultDockDoorSegmentAdjuster(30364.0),
            TieredDefaultDockDoorSegmentAdjuster(
                mutableListOf<Double>(1633.0, 1798.0),
                mutableListOf<Double>(1.11, 0.91),
            ),
            MinimumDefaultDockDoorSegmentAdjuster(33.1),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 9060.0,
    )
}
