package com.acme.transport.network.zone.snapshot

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the zone target calculation. */
interface ZoneTargetModifier {
    fun describe(): String
    fun adjust(item: ZoneTarget, value: Double): Double
}

data class CapZoneTargetAdjuster(var cap: Double) : ZoneTargetModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ZoneTarget, value: Double): Double = minOf(value, cap)
}

data class RoundingZoneTargetAdjuster(var step: Double) : ZoneTargetModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ZoneTarget, value: Double): Double = roundTo(value, step)
}

data class MinimumZoneTargetAdjuster(var minimum: Double) : ZoneTargetModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ZoneTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the zone target steps in order and keeps a trace of each. */
data class ZoneTargetEngine(
    var steps: MutableList<ZoneTargetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ZoneTarget): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ZoneTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultZoneTargetEngine(): ZoneTargetEngine {
    return ZoneTargetEngine(
        steps = mutableListOf<ZoneTargetModifier>(
            CapZoneTargetAdjuster(46417.0),
            RoundingZoneTargetAdjuster(0.05),
            MinimumZoneTargetAdjuster(25.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 95424.5,
    )
}
