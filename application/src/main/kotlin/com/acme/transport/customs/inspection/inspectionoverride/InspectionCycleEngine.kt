package com.acme.transport.customs.inspection.inspectionoverride

import com.acme.transport.kit.flags.Registry

/** One step of the inspection cycle calculation. */
interface InspectionCycleFactor {
    fun describe(): String
    fun adjust(item: InspectionCycle, value: Double): Double
}

data class TieredInspectionCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : InspectionCycleFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: InspectionCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class InspectionCycleSequenceOffsetAdjuster(var weight: Double) : InspectionCycleFactor {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: InspectionCycle, value: Double): Double = value + item.sequence.toDouble() * weight
}

data class CapInspectionCycleAdjuster(var cap: Double) : InspectionCycleFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: InspectionCycle, value: Double): Double = minOf(value, cap)
}

data class MinimumInspectionCycleAdjuster(var minimum: Double) : InspectionCycleFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: InspectionCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedInspectionCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : InspectionCycleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: InspectionCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the inspection cycle steps in order and keeps a trace of each. */
data class InspectionCycleEngine(
    var steps: MutableList<InspectionCycleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: InspectionCycle): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: InspectionCycleFactor = steps[index]
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

    fun evaluateAll(items: MutableList<InspectionCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultInspectionCycleEngine(flags: Registry): InspectionCycleEngine {
    return InspectionCycleEngine(
        steps = mutableListOf<InspectionCycleFactor>(
            TieredInspectionCycleAdjuster(
                mutableListOf<Double>(248.0, 354.0, 858.0),
                mutableListOf<Double>(1.17, 1.19, 1.0),
            ),
            InspectionCycleSequenceOffsetAdjuster(1.2),
            CapInspectionCycleAdjuster(1156.0),
            MinimumInspectionCycleAdjuster(47.5),
            FlaggedInspectionCycleAdjuster(flags, "inspection-cycle-weighted", 1.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 49383.1,
    )
}
