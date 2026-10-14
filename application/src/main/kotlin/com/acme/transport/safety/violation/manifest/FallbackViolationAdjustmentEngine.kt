package com.acme.transport.safety.violation.manifest

/** One step of the fallback violation adjustment calculation. */
interface FallbackViolationAdjustmentStep {
    fun describe(): String
    fun adjust(item: FallbackViolationAdjustment, value: Double): Double
}

data class MinimumFallbackViolationAdjustmentAdjuster(var minimum: Double) : FallbackViolationAdjustmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackViolationAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentFallbackViolationAdjustmentAdjuster(var percent: Double) : FallbackViolationAdjustmentStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackViolationAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FallbackViolationAdjustmentVersionOffsetAdjuster(
    var weight: Double,
) : FallbackViolationAdjustmentStep {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: FallbackViolationAdjustment, value: Double): Double {
        return value + item.version.toDouble() * weight
    }
}

/** Runs the fallback violation adjustment steps in order and keeps a trace of each. */
data class FallbackViolationAdjustmentEngine(
    var steps: MutableList<FallbackViolationAdjustmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FallbackViolationAdjustment): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackViolationAdjustmentStep = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackViolationAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFallbackViolationAdjustmentEngine(): FallbackViolationAdjustmentEngine {
    return FallbackViolationAdjustmentEngine(
        steps = mutableListOf<FallbackViolationAdjustmentStep>(
            MinimumFallbackViolationAdjustmentAdjuster(8.6),
            PercentFallbackViolationAdjustmentAdjuster(10.0),
            FallbackViolationAdjustmentVersionOffsetAdjuster(0.64),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 57203.0,
    )
}
