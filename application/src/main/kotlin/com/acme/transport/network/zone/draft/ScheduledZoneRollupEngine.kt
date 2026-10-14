package com.acme.transport.network.zone.draft

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the scheduled zone rollup calculation. */
interface ScheduledZoneRollupFactor {
    fun describe(): String
    fun adjust(item: ScheduledZoneRollup, value: Double): Double
}

data class ScheduledZoneRollupQuantityOffsetAdjuster(var weight: Double) : ScheduledZoneRollupFactor {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: ScheduledZoneRollup, value: Double): Double = value + item.quantity.toDouble() * weight
}

data class PercentScheduledZoneRollupAdjuster(var percent: Double) : ScheduledZoneRollupFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledZoneRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapScheduledZoneRollupAdjuster(var cap: Double) : ScheduledZoneRollupFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledZoneRollup, value: Double): Double = minOf(value, cap)
}

/** Runs the scheduled zone rollup steps in order and keeps a trace of each. */
data class ScheduledZoneRollupEngine(
    var steps: MutableList<ScheduledZoneRollupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledZoneRollup): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledZoneRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledZoneRollupEngine(): ScheduledZoneRollupEngine {
    return ScheduledZoneRollupEngine(
        steps = mutableListOf<ScheduledZoneRollupFactor>(
            ScheduledZoneRollupQuantityOffsetAdjuster(1.05),
            PercentScheduledZoneRollupAdjuster(19.5),
            CapScheduledZoneRollupAdjuster(11107.0),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 58957.2,
    )
}
