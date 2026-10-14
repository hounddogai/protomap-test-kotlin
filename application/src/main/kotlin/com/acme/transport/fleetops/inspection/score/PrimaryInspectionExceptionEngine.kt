package com.acme.transport.fleetops.inspection.score

/** One step of the primary inspection exception calculation. */
interface PrimaryInspectionExceptionStep {
    fun describe(): String
    fun adjust(item: PrimaryInspectionException, value: Double): Double
}

data class PercentPrimaryInspectionExceptionAdjuster(var percent: Double) : PrimaryInspectionExceptionStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryInspectionException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredPrimaryInspectionExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryInspectionExceptionStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryInspectionException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumPrimaryInspectionExceptionAdjuster(var minimum: Double) : PrimaryInspectionExceptionStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryInspectionException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapPrimaryInspectionExceptionAdjuster(var cap: Double) : PrimaryInspectionExceptionStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryInspectionException, value: Double): Double = minOf(value, cap)
}

/** Runs the primary inspection exception steps in order and keeps a trace of each. */
data class PrimaryInspectionExceptionEngine(
    var steps: MutableList<PrimaryInspectionExceptionStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PrimaryInspectionException): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryInspectionExceptionStep = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryInspectionException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPrimaryInspectionExceptionEngine(): PrimaryInspectionExceptionEngine {
    return PrimaryInspectionExceptionEngine(
        steps = mutableListOf<PrimaryInspectionExceptionStep>(
            PercentPrimaryInspectionExceptionAdjuster(8.5),
            TieredPrimaryInspectionExceptionAdjuster(
                mutableListOf<Double>(17.0, 2940.0),
                mutableListOf<Double>(0.97, 1.19),
            ),
            MinimumPrimaryInspectionExceptionAdjuster(9.1),
            CapPrimaryInspectionExceptionAdjuster(17450.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 68916.3,
    )
}
