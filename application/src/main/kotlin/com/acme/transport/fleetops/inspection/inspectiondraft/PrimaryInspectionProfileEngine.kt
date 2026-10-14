package com.acme.transport.fleetops.inspection.inspectiondraft

import com.acme.transport.kit.numbers.roundTo

/** One step of the primary inspection profile calculation. */
interface PrimaryInspectionProfileStep {
    fun describe(): String
    fun adjust(item: PrimaryInspectionProfile, value: Double): Double
}

data class MinimumPrimaryInspectionProfileAdjuster(var minimum: Double) : PrimaryInspectionProfileStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryInspectionProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapPrimaryInspectionProfileAdjuster(var cap: Double) : PrimaryInspectionProfileStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryInspectionProfile, value: Double): Double = minOf(value, cap)
}

data class TieredPrimaryInspectionProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryInspectionProfileStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryInspectionProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingPrimaryInspectionProfileAdjuster(var step: Double) : PrimaryInspectionProfileStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryInspectionProfile, value: Double): Double = roundTo(value, step)
}

/** Runs the primary inspection profile steps in order and keeps a trace of each. */
data class PrimaryInspectionProfileEngine(
    var steps: MutableList<PrimaryInspectionProfileStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PrimaryInspectionProfile): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryInspectionProfileStep = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryInspectionProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPrimaryInspectionProfileEngine(): PrimaryInspectionProfileEngine {
    return PrimaryInspectionProfileEngine(
        steps = mutableListOf<PrimaryInspectionProfileStep>(
            MinimumPrimaryInspectionProfileAdjuster(28.9),
            CapPrimaryInspectionProfileAdjuster(45296.0),
            TieredPrimaryInspectionProfileAdjuster(
                mutableListOf<Double>(308.0, 4571.0, 9628.0),
                mutableListOf<Double>(0.9, 0.93, 0.94),
            ),
            RoundingPrimaryInspectionProfileAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 2550.6,
    )
}
