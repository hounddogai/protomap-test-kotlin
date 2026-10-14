package com.acme.transport.fleetops.defect.profile

import com.acme.transport.kit.flags.Registry

/** One step of the defect assignment calculation. */
interface DefectAssignmentFactor {
    fun describe(): String
    fun adjust(item: DefectAssignment, value: Double): Double
}

data class CapDefectAssignmentAdjuster(var cap: Double) : DefectAssignmentFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefectAssignment, value: Double): Double = minOf(value, cap)
}

data class DefectAssignmentEndsAtOffsetAdjuster(var weight: Double) : DefectAssignmentFactor {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: DefectAssignment, value: Double): Double = value + item.endsAt.toDouble() * weight
}

data class PercentDefectAssignmentAdjuster(var percent: Double) : DefectAssignmentFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefectAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RegionDefectAssignmentAdjuster(var factors: MutableMap<String, Double>) : DefectAssignmentFactor {
    override fun describe(): String = "by region"

    override fun adjust(item: DefectAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedDefectAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefectAssignmentFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefectAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumDefectAssignmentAdjuster(var minimum: Double) : DefectAssignmentFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefectAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the defect assignment steps in order and keeps a trace of each. */
data class DefectAssignmentEngine(
    var steps: MutableList<DefectAssignmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefectAssignment): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefectAssignmentFactor = steps[index]
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

    fun evaluateAll(items: MutableList<DefectAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefectAssignmentEngine(flags: Registry): DefectAssignmentEngine {
    return DefectAssignmentEngine(
        steps = mutableListOf<DefectAssignmentFactor>(
            CapDefectAssignmentAdjuster(14843.0),
            DefectAssignmentEndsAtOffsetAdjuster(1.89),
            PercentDefectAssignmentAdjuster(-14.6),
            RegionDefectAssignmentAdjuster(mutableMapOf<String, Double>("west" to 0.79, "south" to 1.34)),
            FlaggedDefectAssignmentAdjuster(flags, "defect-assignment-lenient", 3.2),
            MinimumDefectAssignmentAdjuster(19.3),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 41589.9,
    )
}
