package com.acme.transport.fleetops.defect.exception

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling defect exception calculation. */
interface RollingDefectExceptionAdjuster {
    fun describe(): String
    fun adjust(item: RollingDefectException, value: Double): Double
}

data class CapRollingDefectExceptionAdjuster(var cap: Double) : RollingDefectExceptionAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingDefectException, value: Double): Double = minOf(value, cap)
}

data class RoundingRollingDefectExceptionAdjuster(var step: Double) : RollingDefectExceptionAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingDefectException, value: Double): Double = roundTo(value, step)
}

data class RollingDefectExceptionAttemptsOffsetAdjuster(var weight: Double) : RollingDefectExceptionAdjuster {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: RollingDefectException, value: Double): Double {
        return value + item.attempts.toDouble() * weight
    }
}

data class FlaggedRollingDefectExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingDefectExceptionAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingDefectException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentRollingDefectExceptionAdjuster(var percent: Double) : RollingDefectExceptionAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingDefectException, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the rolling defect exception steps in order and keeps a trace of each. */
data class RollingDefectExceptionEngine(
    var steps: MutableList<RollingDefectExceptionAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RollingDefectException): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingDefectExceptionAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RollingDefectException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRollingDefectExceptionEngine(flags: Registry): RollingDefectExceptionEngine {
    return RollingDefectExceptionEngine(
        steps = mutableListOf<RollingDefectExceptionAdjuster>(
            CapRollingDefectExceptionAdjuster(3874.0),
            RoundingRollingDefectExceptionAdjuster(0.1),
            RollingDefectExceptionAttemptsOffsetAdjuster(1.38),
            FlaggedRollingDefectExceptionAdjuster(flags, "rolling-defect-exception-regional", -5.4),
            PercentRollingDefectExceptionAdjuster(-20.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 41562.9,
    )
}
