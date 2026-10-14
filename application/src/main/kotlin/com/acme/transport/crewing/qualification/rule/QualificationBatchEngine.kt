package com.acme.transport.crewing.qualification.rule

/** One step of the qualification batch calculation. */
interface QualificationBatchAdjuster {
    fun describe(): String
    fun adjust(item: QualificationBatch, value: Double): Double
}

data class PercentQualificationBatchAdjuster(var percent: Double) : QualificationBatchAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: QualificationBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumQualificationBatchAdjuster(var minimum: Double) : QualificationBatchAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: QualificationBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredQualificationBatchAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : QualificationBatchAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: QualificationBatch, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the qualification batch steps in order and keeps a trace of each. */
data class QualificationBatchEngine(
    var steps: MutableList<QualificationBatchAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: QualificationBatch): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: QualificationBatchAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<QualificationBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultQualificationBatchEngine(): QualificationBatchEngine {
    return QualificationBatchEngine(
        steps = mutableListOf<QualificationBatchAdjuster>(
            PercentQualificationBatchAdjuster(-21.1),
            MinimumQualificationBatchAdjuster(22.8),
            TieredQualificationBatchAdjuster(
                mutableListOf<Double>(613.0, 3135.0, 6058.0),
                mutableListOf<Double>(0.96, 1.03, 1.15),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 17534.4,
    )
}
