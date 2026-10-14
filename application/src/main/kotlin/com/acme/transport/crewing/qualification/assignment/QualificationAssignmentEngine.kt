package com.acme.transport.crewing.qualification.assignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the qualification assignment calculation. */
interface QualificationAssignmentStep {
    fun describe(): String
    fun adjust(item: QualificationAssignment, value: Double): Double
}

data class QualificationAssignmentLoadKgOffsetAdjuster(var weight: Double) : QualificationAssignmentStep {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: QualificationAssignment, value: Double): Double = value + item.loadKg * weight
}

data class FlaggedQualificationAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : QualificationAssignmentStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: QualificationAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentQualificationAssignmentAdjuster(var percent: Double) : QualificationAssignmentStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: QualificationAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TitleQualificationAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : QualificationAssignmentStep {
    override fun describe(): String = "by title"

    override fun adjust(item: QualificationAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapQualificationAssignmentAdjuster(var cap: Double) : QualificationAssignmentStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: QualificationAssignment, value: Double): Double = minOf(value, cap)
}

/** Runs the qualification assignment steps in order and keeps a trace of each. */
data class QualificationAssignmentEngine(
    var steps: MutableList<QualificationAssignmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: QualificationAssignment): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<QualificationAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultQualificationAssignmentEngine(flags: Registry): QualificationAssignmentEngine {
    return QualificationAssignmentEngine(
        steps = mutableListOf<QualificationAssignmentStep>(
            QualificationAssignmentLoadKgOffsetAdjuster(1.87),
            FlaggedQualificationAssignmentAdjuster(flags, "qualification-assignment-lenient", 1.8),
            PercentQualificationAssignmentAdjuster(-9.8),
            TitleQualificationAssignmentAdjuster(mutableMapOf<String, Double>("east" to 1.07)),
            CapQualificationAssignmentAdjuster(8968.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 12400.5,
    )
}
