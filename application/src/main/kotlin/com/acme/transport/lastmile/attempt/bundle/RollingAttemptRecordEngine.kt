package com.acme.transport.lastmile.attempt.bundle

import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling attempt record calculation. */
interface RollingAttemptRecordStep {
    fun describe(): String
    fun adjust(item: RollingAttemptRecord, value: Double): Double
}

data class CapRollingAttemptRecordAdjuster(var cap: Double) : RollingAttemptRecordStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingAttemptRecord, value: Double): Double = minOf(value, cap)
}

data class RollingAttemptRecordThresholdOffsetAdjuster(var weight: Double) : RollingAttemptRecordStep {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: RollingAttemptRecord, value: Double): Double = value + item.threshold * weight
}

data class RoundingRollingAttemptRecordAdjuster(var step: Double) : RollingAttemptRecordStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingAttemptRecord, value: Double): Double = roundTo(value, step)
}

data class PercentRollingAttemptRecordAdjuster(var percent: Double) : RollingAttemptRecordStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingAttemptRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class DepotRollingAttemptRecordAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingAttemptRecordStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: RollingAttemptRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumRollingAttemptRecordAdjuster(var minimum: Double) : RollingAttemptRecordStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingAttemptRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the rolling attempt record steps in order and keeps a trace of each. */
data class RollingAttemptRecordEngine(
    var steps: MutableList<RollingAttemptRecordStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingAttemptRecord): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingAttemptRecordStep = steps[index]
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

    fun evaluateAll(items: MutableList<RollingAttemptRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingAttemptRecordEngine(): RollingAttemptRecordEngine {
    return RollingAttemptRecordEngine(
        steps = mutableListOf<RollingAttemptRecordStep>(
            CapRollingAttemptRecordAdjuster(22661.0),
            RollingAttemptRecordThresholdOffsetAdjuster(0.85),
            RoundingRollingAttemptRecordAdjuster(5.0),
            PercentRollingAttemptRecordAdjuster(-11.5),
            DepotRollingAttemptRecordAdjuster(mutableMapOf<String, Double>("south" to 0.75, "west" to 1.24)),
            MinimumRollingAttemptRecordAdjuster(48.6),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 88732.5,
    )
}
