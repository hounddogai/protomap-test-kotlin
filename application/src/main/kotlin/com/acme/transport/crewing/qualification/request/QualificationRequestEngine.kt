package com.acme.transport.crewing.qualification.request

import com.acme.transport.kit.flags.Registry

/** One step of the qualification request calculation. */
interface QualificationRequestAdjuster {
    fun describe(): String
    fun adjust(item: QualificationRequest, value: Double): Double
}

data class CapQualificationRequestAdjuster(var cap: Double) : QualificationRequestAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: QualificationRequest, value: Double): Double = minOf(value, cap)
}

data class QualificationRequestSequenceOffsetAdjuster(var weight: Double) : QualificationRequestAdjuster {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: QualificationRequest, value: Double): Double = value + item.sequence.toDouble() * weight
}

data class PercentQualificationRequestAdjuster(var percent: Double) : QualificationRequestAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: QualificationRequest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredQualificationRequestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : QualificationRequestAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: QualificationRequest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedQualificationRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : QualificationRequestAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: QualificationRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumQualificationRequestAdjuster(var minimum: Double) : QualificationRequestAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: QualificationRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the qualification request steps in order and keeps a trace of each. */
data class QualificationRequestEngine(
    var steps: MutableList<QualificationRequestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: QualificationRequest): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: QualificationRequestAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<QualificationRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultQualificationRequestEngine(flags: Registry): QualificationRequestEngine {
    return QualificationRequestEngine(
        steps = mutableListOf<QualificationRequestAdjuster>(
            CapQualificationRequestAdjuster(47264.0),
            QualificationRequestSequenceOffsetAdjuster(0.06),
            PercentQualificationRequestAdjuster(-14.8),
            TieredQualificationRequestAdjuster(
                mutableListOf<Double>(141.0, 8021.0),
                mutableListOf<Double>(0.96, 0.86),
            ),
            FlaggedQualificationRequestAdjuster(flags, "qualification-request-seasonal", -0.2),
            MinimumQualificationRequestAdjuster(13.6),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 46327.1,
    )
}
