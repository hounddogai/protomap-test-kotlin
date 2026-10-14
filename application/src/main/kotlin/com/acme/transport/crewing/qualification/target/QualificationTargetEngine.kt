package com.acme.transport.crewing.qualification.target

import com.acme.transport.kit.flags.Registry

/** One step of the qualification target calculation. */
interface QualificationTargetFactor {
    fun describe(): String
    fun adjust(item: QualificationTarget, value: Double): Double
}

data class FlaggedQualificationTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : QualificationTargetFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: QualificationTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapQualificationTargetAdjuster(var cap: Double) : QualificationTargetFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: QualificationTarget, value: Double): Double = minOf(value, cap)
}

data class MinimumQualificationTargetAdjuster(var minimum: Double) : QualificationTargetFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: QualificationTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class SourceQualificationTargetAdjuster(
    var factors: MutableMap<String, Double>,
) : QualificationTargetFactor {
    override fun describe(): String = "by source"

    override fun adjust(item: QualificationTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentQualificationTargetAdjuster(var percent: Double) : QualificationTargetFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: QualificationTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class QualificationTargetRatioOffsetAdjuster(var weight: Double) : QualificationTargetFactor {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: QualificationTarget, value: Double): Double = value + item.ratio * weight
}

/** Runs the qualification target steps in order and keeps a trace of each. */
data class QualificationTargetEngine(
    var steps: MutableList<QualificationTargetFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: QualificationTarget): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: QualificationTargetFactor = steps[index]
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

    fun evaluateAll(items: MutableList<QualificationTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultQualificationTargetEngine(flags: Registry): QualificationTargetEngine {
    return QualificationTargetEngine(
        steps = mutableListOf<QualificationTargetFactor>(
            FlaggedQualificationTargetAdjuster(flags, "qualification-target-manual", -4.6),
            CapQualificationTargetAdjuster(20126.0),
            MinimumQualificationTargetAdjuster(9.6),
            SourceQualificationTargetAdjuster(
                mutableMapOf<String, Double>("online" to 0.8, "retail" to 1.06, "central" to 1.34),
            ),
            PercentQualificationTargetAdjuster(-22.0),
            QualificationTargetRatioOffsetAdjuster(0.69),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 48611.9,
    )
}
