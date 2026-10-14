package com.acme.transport.crewing.qualification.estimate

import com.acme.transport.kit.numbers.roundTo

/** One step of the qualification estimate calculation. */
interface QualificationEstimateModifier {
    fun describe(): String
    fun adjust(item: QualificationEstimate, value: Double): Double
}

data class RoundingQualificationEstimateAdjuster(var step: Double) : QualificationEstimateModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: QualificationEstimate, value: Double): Double = roundTo(value, step)
}

data class PercentQualificationEstimateAdjuster(var percent: Double) : QualificationEstimateModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: QualificationEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredQualificationEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : QualificationEstimateModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: QualificationEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class QualificationEstimatePalletsOffsetAdjuster(var weight: Double) : QualificationEstimateModifier {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: QualificationEstimate, value: Double): Double = value + item.pallets.toDouble() * weight
}

data class LocaleQualificationEstimateAdjuster(
    var factors: MutableMap<String, Double>,
) : QualificationEstimateModifier {
    override fun describe(): String = "by locale"

    override fun adjust(item: QualificationEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumQualificationEstimateAdjuster(var minimum: Double) : QualificationEstimateModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: QualificationEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the qualification estimate steps in order and keeps a trace of each. */
data class QualificationEstimateEngine(
    var steps: MutableList<QualificationEstimateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: QualificationEstimate): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: QualificationEstimateModifier = steps[index]
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

    fun evaluateAll(items: MutableList<QualificationEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultQualificationEstimateEngine(): QualificationEstimateEngine {
    return QualificationEstimateEngine(
        steps = mutableListOf<QualificationEstimateModifier>(
            RoundingQualificationEstimateAdjuster(0.05),
            PercentQualificationEstimateAdjuster(10.6),
            TieredQualificationEstimateAdjuster(
                mutableListOf<Double>(505.0, 4148.0, 7585.0, 7649.0),
                mutableListOf<Double>(1.01, 1.09, 1.12, 0.9),
            ),
            QualificationEstimatePalletsOffsetAdjuster(1.24),
            LocaleQualificationEstimateAdjuster(mutableMapOf<String, Double>("north" to 0.99, "south" to 1.32)),
            MinimumQualificationEstimateAdjuster(12.8),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 53689.1,
    )
}
