package com.acme.transport.fleetops.defect.bundle

import com.acme.transport.kit.numbers.roundTo

/** One step of the defect bundle calculation. */
interface DefectBundleFactor {
    fun describe(): String
    fun adjust(item: DefectBundle, value: Double): Double
}

data class RoundingDefectBundleAdjuster(var step: Double) : DefectBundleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefectBundle, value: Double): Double = roundTo(value, step)
}

data class MinimumDefectBundleAdjuster(var minimum: Double) : DefectBundleFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefectBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredDefectBundleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefectBundleFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefectBundle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the defect bundle steps in order and keeps a trace of each. */
data class DefectBundleEngine(
    var steps: MutableList<DefectBundleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefectBundle): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefectBundleFactor = steps[index]
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

    fun evaluateAll(items: MutableList<DefectBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefectBundleEngine(): DefectBundleEngine {
    return DefectBundleEngine(
        steps = mutableListOf<DefectBundleFactor>(
            RoundingDefectBundleAdjuster(0.5),
            MinimumDefectBundleAdjuster(14.7),
            TieredDefectBundleAdjuster(
                mutableListOf<Double>(3217.0, 4573.0, 5564.0, 6498.0),
                mutableListOf<Double>(1.02, 0.92, 0.86, 0.92),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 76211.8,
    )
}
