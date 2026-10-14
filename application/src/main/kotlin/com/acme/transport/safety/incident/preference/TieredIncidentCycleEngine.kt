package com.acme.transport.safety.incident.preference

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered incident cycle calculation. */
interface TieredIncidentCycleStep {
    fun describe(): String
    fun adjust(item: TieredIncidentCycle, value: Double): Double
}

data class PercentTieredIncidentCycleAdjuster(var percent: Double) : TieredIncidentCycleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredIncidentCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumTieredIncidentCycleAdjuster(var minimum: Double) : TieredIncidentCycleStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredIncidentCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapTieredIncidentCycleAdjuster(var cap: Double) : TieredIncidentCycleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredIncidentCycle, value: Double): Double = minOf(value, cap)
}

data class RoundingTieredIncidentCycleAdjuster(var step: Double) : TieredIncidentCycleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredIncidentCycle, value: Double): Double = roundTo(value, step)
}

data class TieredIncidentCycleMinimumOffsetAdjuster(var weight: Double) : TieredIncidentCycleStep {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: TieredIncidentCycle, value: Double): Double = value + item.minimum * weight
}

/** Runs the tiered incident cycle steps in order and keeps a trace of each. */
data class TieredIncidentCycleEngine(
    var steps: MutableList<TieredIncidentCycleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TieredIncidentCycle): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredIncidentCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTieredIncidentCycleEngine(): TieredIncidentCycleEngine {
    return TieredIncidentCycleEngine(
        steps = mutableListOf<TieredIncidentCycleStep>(
            PercentTieredIncidentCycleAdjuster(-18.6),
            MinimumTieredIncidentCycleAdjuster(33.8),
            CapTieredIncidentCycleAdjuster(10948.0),
            RoundingTieredIncidentCycleAdjuster(1.0),
            TieredIncidentCycleMinimumOffsetAdjuster(0.65),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 45479.5,
    )
}
