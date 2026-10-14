package com.acme.transport.customs.inspection.target

import com.acme.transport.kit.flags.Registry

/** One step of the inspection target calculation. */
interface InspectionTargetStep {
    fun describe(): String
    fun adjust(item: InspectionTarget, value: Double): Double
}

data class TitleInspectionTargetAdjuster(var factors: MutableMap<String, Double>) : InspectionTargetStep {
    override fun describe(): String = "by title"

    override fun adjust(item: InspectionTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedInspectionTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : InspectionTargetStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: InspectionTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredInspectionTargetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : InspectionTargetStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: InspectionTarget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class InspectionTargetCeilingOffsetAdjuster(var weight: Double) : InspectionTargetStep {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: InspectionTarget, value: Double): Double = value + item.ceiling * weight
}

/** Runs the inspection target steps in order and keeps a trace of each. */
data class InspectionTargetEngine(
    var steps: MutableList<InspectionTargetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: InspectionTarget): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: InspectionTargetStep = steps[index]
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

    fun evaluateAll(items: MutableList<InspectionTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultInspectionTargetEngine(flags: Registry): InspectionTargetEngine {
    return InspectionTargetEngine(
        steps = mutableListOf<InspectionTargetStep>(
            TitleInspectionTargetAdjuster(mutableMapOf<String, Double>("retail" to 0.8, "south" to 1.26)),
            FlaggedInspectionTargetAdjuster(flags, "inspection-target-rolling", 4.8),
            TieredInspectionTargetAdjuster(
                mutableListOf<Double>(131.0, 4089.0, 6024.0),
                mutableListOf<Double>(1.17, 1.01, 0.92),
            ),
            InspectionTargetCeilingOffsetAdjuster(0.23),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 1097.8,
    )
}
