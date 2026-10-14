package com.acme.transport.lastmile.parcel.ledger

import com.acme.transport.kit.flags.Registry

/** One step of the parcel assignment calculation. */
interface ParcelAssignmentStep {
    fun describe(): String
    fun adjust(item: ParcelAssignment, value: Double): Double
}

data class FlaggedParcelAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ParcelAssignmentStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ParcelAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredParcelAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ParcelAssignmentStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ParcelAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class ParcelAssignmentMaximumOffsetAdjuster(var weight: Double) : ParcelAssignmentStep {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: ParcelAssignment, value: Double): Double = value + item.maximum * weight
}

/** Runs the parcel assignment steps in order and keeps a trace of each. */
data class ParcelAssignmentEngine(
    var steps: MutableList<ParcelAssignmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ParcelAssignment): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ParcelAssignmentStep = steps[index]
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

    fun evaluateAll(items: MutableList<ParcelAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultParcelAssignmentEngine(flags: Registry): ParcelAssignmentEngine {
    return ParcelAssignmentEngine(
        steps = mutableListOf<ParcelAssignmentStep>(
            FlaggedParcelAssignmentAdjuster(flags, "parcel-assignment-fallback", 4.7),
            TieredParcelAssignmentAdjuster(
                mutableListOf<Double>(727.0, 6541.0, 9448.0),
                mutableListOf<Double>(0.87, 0.86, 0.84),
            ),
            ParcelAssignmentMaximumOffsetAdjuster(0.84),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 19012.3,
    )
}
