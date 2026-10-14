package com.acme.transport.terminal.dockdoor.rule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the dock door estimate calculation. */
interface DockDoorEstimateModifier {
    fun describe(): String
    fun adjust(item: DockDoorEstimate, value: Double): Double
}

data class RoundingDockDoorEstimateAdjuster(var step: Double) : DockDoorEstimateModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DockDoorEstimate, value: Double): Double = roundTo(value, step)
}

data class FlaggedDockDoorEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DockDoorEstimateModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DockDoorEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LaneDockDoorEstimateAdjuster(var factors: MutableMap<String, Double>) : DockDoorEstimateModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: DockDoorEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the dock door estimate steps in order and keeps a trace of each. */
data class DockDoorEstimateEngine(
    var steps: MutableList<DockDoorEstimateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DockDoorEstimate): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DockDoorEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDockDoorEstimateEngine(flags: Registry): DockDoorEstimateEngine {
    return DockDoorEstimateEngine(
        steps = mutableListOf<DockDoorEstimateModifier>(
            RoundingDockDoorEstimateAdjuster(1.0),
            FlaggedDockDoorEstimateAdjuster(flags, "dock-door-estimate-adaptive", 3.3),
            LaneDockDoorEstimateAdjuster(mutableMapOf<String, Double>("south" to 0.87)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 63417.2,
    )
}
