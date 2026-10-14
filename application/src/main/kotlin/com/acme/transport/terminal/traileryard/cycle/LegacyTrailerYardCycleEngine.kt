package com.acme.transport.terminal.traileryard.cycle

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the legacy trailer yard cycle calculation. */
interface LegacyTrailerYardCycleAdjuster {
    fun describe(): String
    fun adjust(item: LegacyTrailerYardCycle, value: Double): Double
}

data class MinimumLegacyTrailerYardCycleAdjuster(var minimum: Double) : LegacyTrailerYardCycleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyTrailerYardCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapLegacyTrailerYardCycleAdjuster(var cap: Double) : LegacyTrailerYardCycleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyTrailerYardCycle, value: Double): Double = minOf(value, cap)
}

data class RoundingLegacyTrailerYardCycleAdjuster(var step: Double) : LegacyTrailerYardCycleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyTrailerYardCycle, value: Double): Double = roundTo(value, step)
}

/** Runs the legacy trailer yard cycle steps in order and keeps a trace of each. */
data class LegacyTrailerYardCycleEngine(
    var steps: MutableList<LegacyTrailerYardCycleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyTrailerYardCycle): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyTrailerYardCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyTrailerYardCycleEngine(): LegacyTrailerYardCycleEngine {
    return LegacyTrailerYardCycleEngine(
        steps = mutableListOf<LegacyTrailerYardCycleAdjuster>(
            MinimumLegacyTrailerYardCycleAdjuster(10.4),
            CapLegacyTrailerYardCycleAdjuster(24189.0),
            RoundingLegacyTrailerYardCycleAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 35292.6,
    )
}
