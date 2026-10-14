package com.acme.transport.fleetops.inspection.inspectionbatch

import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling inspection draft calculation. */
interface RollingInspectionDraftFactor {
    fun describe(): String
    fun adjust(item: RollingInspectionDraft, value: Double): Double
}

data class RoundingRollingInspectionDraftAdjuster(var step: Double) : RollingInspectionDraftFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingInspectionDraft, value: Double): Double = roundTo(value, step)
}

data class RollingInspectionDraftFuelLitersOffsetAdjuster(var weight: Double) : RollingInspectionDraftFactor {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: RollingInspectionDraft, value: Double): Double = value + item.fuelLiters * weight
}

data class TieredRollingInspectionDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingInspectionDraftFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingInspectionDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TitleRollingInspectionDraftAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingInspectionDraftFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: RollingInspectionDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapRollingInspectionDraftAdjuster(var cap: Double) : RollingInspectionDraftFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingInspectionDraft, value: Double): Double = minOf(value, cap)
}

/** Runs the rolling inspection draft steps in order and keeps a trace of each. */
data class RollingInspectionDraftEngine(
    var steps: MutableList<RollingInspectionDraftFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RollingInspectionDraft): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingInspectionDraftFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RollingInspectionDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRollingInspectionDraftEngine(): RollingInspectionDraftEngine {
    return RollingInspectionDraftEngine(
        steps = mutableListOf<RollingInspectionDraftFactor>(
            RoundingRollingInspectionDraftAdjuster(0.1),
            RollingInspectionDraftFuelLitersOffsetAdjuster(1.57),
            TieredRollingInspectionDraftAdjuster(
                mutableListOf<Double>(2406.0, 3485.0, 7482.0, 7581.0),
                mutableListOf<Double>(1.19, 1.02, 1.1, 0.81),
            ),
            TitleRollingInspectionDraftAdjuster(mutableMapOf<String, Double>("online" to 0.94)),
            CapRollingInspectionDraftAdjuster(14245.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 14512.9,
    )
}
