package com.acme.transport.crewing.qualification.bundle

import com.acme.transport.kit.numbers.roundTo

/** One step of the qualification manifest calculation. */
interface QualificationManifestModifier {
    fun describe(): String
    fun adjust(item: QualificationManifest, value: Double): Double
}

data class MinimumQualificationManifestAdjuster(var minimum: Double) : QualificationManifestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: QualificationManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingQualificationManifestAdjuster(var step: Double) : QualificationManifestModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: QualificationManifest, value: Double): Double = roundTo(value, step)
}

data class QualificationManifestCostOffsetAdjuster(var weight: Double) : QualificationManifestModifier {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: QualificationManifest, value: Double): Double = value + item.cost * weight
}

/** Runs the qualification manifest steps in order and keeps a trace of each. */
data class QualificationManifestEngine(
    var steps: MutableList<QualificationManifestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: QualificationManifest): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: QualificationManifestModifier = steps[index]
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

    fun evaluateAll(items: MutableList<QualificationManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultQualificationManifestEngine(): QualificationManifestEngine {
    return QualificationManifestEngine(
        steps = mutableListOf<QualificationManifestModifier>(
            MinimumQualificationManifestAdjuster(7.6),
            RoundingQualificationManifestAdjuster(0.5),
            QualificationManifestCostOffsetAdjuster(0.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 27320.9,
    )
}
