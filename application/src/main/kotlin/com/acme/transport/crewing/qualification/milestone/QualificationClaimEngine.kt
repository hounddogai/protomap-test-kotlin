package com.acme.transport.crewing.qualification.milestone

import com.acme.transport.kit.numbers.roundTo

/** One step of the qualification claim calculation. */
interface QualificationClaimModifier {
    fun describe(): String
    fun adjust(item: QualificationClaim, value: Double): Double
}

data class RoundingQualificationClaimAdjuster(var step: Double) : QualificationClaimModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: QualificationClaim, value: Double): Double = roundTo(value, step)
}

data class MinimumQualificationClaimAdjuster(var minimum: Double) : QualificationClaimModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: QualificationClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredQualificationClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : QualificationClaimModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: QualificationClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapQualificationClaimAdjuster(var cap: Double) : QualificationClaimModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: QualificationClaim, value: Double): Double = minOf(value, cap)
}

data class PercentQualificationClaimAdjuster(var percent: Double) : QualificationClaimModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: QualificationClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the qualification claim steps in order and keeps a trace of each. */
data class QualificationClaimEngine(
    var steps: MutableList<QualificationClaimModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: QualificationClaim): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: QualificationClaimModifier = steps[index]
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

    fun evaluateAll(items: MutableList<QualificationClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultQualificationClaimEngine(): QualificationClaimEngine {
    return QualificationClaimEngine(
        steps = mutableListOf<QualificationClaimModifier>(
            RoundingQualificationClaimAdjuster(0.05),
            MinimumQualificationClaimAdjuster(21.5),
            TieredQualificationClaimAdjuster(
                mutableListOf<Double>(4325.0, 8418.0),
                mutableListOf<Double>(0.92, 0.83),
            ),
            CapQualificationClaimAdjuster(38839.0),
            PercentQualificationClaimAdjuster(-8.8),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 13505.9,
    )
}
