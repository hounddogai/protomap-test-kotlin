package com.acme.transport.customs.inspection.inspectiontarget

import com.acme.transport.kit.numbers.roundTo

/** One step of the inspection manifest calculation. */
interface InspectionManifestAdjuster {
    fun describe(): String
    fun adjust(item: InspectionManifest, value: Double): Double
}

data class InspectionManifestRevisionOffsetAdjuster(var weight: Double) : InspectionManifestAdjuster {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: InspectionManifest, value: Double): Double = value + item.revision.toDouble() * weight
}

data class CapInspectionManifestAdjuster(var cap: Double) : InspectionManifestAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: InspectionManifest, value: Double): Double = minOf(value, cap)
}

data class SegmentInspectionManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : InspectionManifestAdjuster {
    override fun describe(): String = "by segment"

    override fun adjust(item: InspectionManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentInspectionManifestAdjuster(var percent: Double) : InspectionManifestAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: InspectionManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingInspectionManifestAdjuster(var step: Double) : InspectionManifestAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: InspectionManifest, value: Double): Double = roundTo(value, step)
}

data class MinimumInspectionManifestAdjuster(var minimum: Double) : InspectionManifestAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: InspectionManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the inspection manifest steps in order and keeps a trace of each. */
data class InspectionManifestEngine(
    var steps: MutableList<InspectionManifestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: InspectionManifest): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: InspectionManifestAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<InspectionManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultInspectionManifestEngine(): InspectionManifestEngine {
    return InspectionManifestEngine(
        steps = mutableListOf<InspectionManifestAdjuster>(
            InspectionManifestRevisionOffsetAdjuster(1.03),
            CapInspectionManifestAdjuster(16068.0),
            SegmentInspectionManifestAdjuster(mutableMapOf<String, Double>("south" to 1.38)),
            PercentInspectionManifestAdjuster(-9.0),
            RoundingInspectionManifestAdjuster(5.0),
            MinimumInspectionManifestAdjuster(41.7),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 48153.9,
    )
}
