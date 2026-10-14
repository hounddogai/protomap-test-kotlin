package com.acme.transport.network.zone.draft

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global zone target calculation. */
interface GlobalZoneTargetFactor {
    fun describe(): String
    fun adjust(item: GlobalZoneTarget, value: Double): Double
}

data class RoundingGlobalZoneTargetAdjuster(var step: Double) : GlobalZoneTargetFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalZoneTarget, value: Double): Double = roundTo(value, step)
}

data class LocaleGlobalZoneTargetAdjuster(var factors: MutableMap<String, Double>) : GlobalZoneTargetFactor {
    override fun describe(): String = "by locale"

    override fun adjust(item: GlobalZoneTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapGlobalZoneTargetAdjuster(var cap: Double) : GlobalZoneTargetFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalZoneTarget, value: Double): Double = minOf(value, cap)
}

data class GlobalZoneTargetCostOffsetAdjuster(var weight: Double) : GlobalZoneTargetFactor {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: GlobalZoneTarget, value: Double): Double = value + item.cost * weight
}

data class PercentGlobalZoneTargetAdjuster(var percent: Double) : GlobalZoneTargetFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalZoneTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the global zone target steps in order and keeps a trace of each. */
data class GlobalZoneTargetEngine(
    var steps: MutableList<GlobalZoneTargetFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: GlobalZoneTarget): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalZoneTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultGlobalZoneTargetEngine(): GlobalZoneTargetEngine {
    return GlobalZoneTargetEngine(
        steps = mutableListOf<GlobalZoneTargetFactor>(
            RoundingGlobalZoneTargetAdjuster(0.5),
            LocaleGlobalZoneTargetAdjuster(mutableMapOf<String, Double>("online" to 0.96, "north" to 1.14)),
            CapGlobalZoneTargetAdjuster(33134.0),
            GlobalZoneTargetCostOffsetAdjuster(1.67),
            PercentGlobalZoneTargetAdjuster(-23.9),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 61050.8,
    )
}
