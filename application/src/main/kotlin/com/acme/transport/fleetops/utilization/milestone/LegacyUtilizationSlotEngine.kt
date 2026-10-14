package com.acme.transport.fleetops.utilization.milestone

import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy utilization slot calculation. */
interface LegacyUtilizationSlotStep {
    fun describe(): String
    fun adjust(item: LegacyUtilizationSlot, value: Double): Double
}

data class MinimumLegacyUtilizationSlotAdjuster(var minimum: Double) : LegacyUtilizationSlotStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyUtilizationSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LegacyUtilizationSlotRateOffsetAdjuster(var weight: Double) : LegacyUtilizationSlotStep {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: LegacyUtilizationSlot, value: Double): Double = value + item.rate * weight
}

data class RoundingLegacyUtilizationSlotAdjuster(var step: Double) : LegacyUtilizationSlotStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyUtilizationSlot, value: Double): Double = roundTo(value, step)
}

/** Runs the legacy utilization slot steps in order and keeps a trace of each. */
data class LegacyUtilizationSlotEngine(
    var steps: MutableList<LegacyUtilizationSlotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LegacyUtilizationSlot): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyUtilizationSlotStep = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyUtilizationSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLegacyUtilizationSlotEngine(): LegacyUtilizationSlotEngine {
    return LegacyUtilizationSlotEngine(
        steps = mutableListOf<LegacyUtilizationSlotStep>(
            MinimumLegacyUtilizationSlotAdjuster(22.3),
            LegacyUtilizationSlotRateOffsetAdjuster(1.41),
            RoundingLegacyUtilizationSlotAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 85224.1,
    )
}
