package com.acme.transport.fleetops.defect.defectmapping

import com.acme.transport.kit.numbers.roundTo

/** One step of the weighted defect manifest calculation. */
interface WeightedDefectManifestStep {
    fun describe(): String
    fun adjust(item: WeightedDefectManifest, value: Double): Double
}

data class LabelWeightedDefectManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedDefectManifestStep {
    override fun describe(): String = "by label"

    override fun adjust(item: WeightedDefectManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentWeightedDefectManifestAdjuster(var percent: Double) : WeightedDefectManifestStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedDefectManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingWeightedDefectManifestAdjuster(var step: Double) : WeightedDefectManifestStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedDefectManifest, value: Double): Double = roundTo(value, step)
}

data class MinimumWeightedDefectManifestAdjuster(var minimum: Double) : WeightedDefectManifestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedDefectManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the weighted defect manifest steps in order and keeps a trace of each. */
data class WeightedDefectManifestEngine(
    var steps: MutableList<WeightedDefectManifestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: WeightedDefectManifest): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedDefectManifestStep = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedDefectManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultWeightedDefectManifestEngine(): WeightedDefectManifestEngine {
    return WeightedDefectManifestEngine(
        steps = mutableListOf<WeightedDefectManifestStep>(
            LabelWeightedDefectManifestAdjuster(
                mutableMapOf<String, Double>("east" to 1.11, "online" to 0.77, "north" to 1.34),
            ),
            PercentWeightedDefectManifestAdjuster(-24.3),
            RoundingWeightedDefectManifestAdjuster(5.0),
            MinimumWeightedDefectManifestAdjuster(25.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 43344.1,
    )
}
