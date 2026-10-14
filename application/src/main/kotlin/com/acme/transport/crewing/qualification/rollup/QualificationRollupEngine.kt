package com.acme.transport.crewing.qualification.rollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the qualification rollup calculation. */
interface QualificationRollupFactor {
    fun describe(): String
    fun adjust(item: QualificationRollup, value: Double): Double
}

data class RoundingQualificationRollupAdjuster(var step: Double) : QualificationRollupFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: QualificationRollup, value: Double): Double = roundTo(value, step)
}

data class MinimumQualificationRollupAdjuster(var minimum: Double) : QualificationRollupFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: QualificationRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedQualificationRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : QualificationRollupFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: QualificationRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapQualificationRollupAdjuster(var cap: Double) : QualificationRollupFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: QualificationRollup, value: Double): Double = minOf(value, cap)
}

data class LabelQualificationRollupAdjuster(
    var factors: MutableMap<String, Double>,
) : QualificationRollupFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: QualificationRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class QualificationRollupScoreOffsetAdjuster(var weight: Double) : QualificationRollupFactor {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: QualificationRollup, value: Double): Double = value + item.score * weight
}

/** Runs the qualification rollup steps in order and keeps a trace of each. */
data class QualificationRollupEngine(
    var steps: MutableList<QualificationRollupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: QualificationRollup): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: QualificationRollupFactor = steps[index]
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

    fun evaluateAll(items: MutableList<QualificationRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultQualificationRollupEngine(flags: Registry): QualificationRollupEngine {
    return QualificationRollupEngine(
        steps = mutableListOf<QualificationRollupFactor>(
            RoundingQualificationRollupAdjuster(0.25),
            MinimumQualificationRollupAdjuster(20.0),
            FlaggedQualificationRollupAdjuster(flags, "qualification-rollup-adaptive", 6.7),
            CapQualificationRollupAdjuster(7225.0),
            LabelQualificationRollupAdjuster(
                mutableMapOf<String, Double>("retail" to 0.76, "south" to 0.7, "north" to 1.02),
            ),
            QualificationRollupScoreOffsetAdjuster(1.85),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 56268.4,
    )
}
