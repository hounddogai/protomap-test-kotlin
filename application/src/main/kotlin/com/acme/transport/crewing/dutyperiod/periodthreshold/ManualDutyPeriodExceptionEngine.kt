package com.acme.transport.crewing.dutyperiod.periodthreshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the manual duty period exception calculation. */
interface ManualDutyPeriodExceptionFactor {
    fun describe(): String
    fun adjust(item: ManualDutyPeriodException, value: Double): Double
}

data class MinimumManualDutyPeriodExceptionAdjuster(var minimum: Double) : ManualDutyPeriodExceptionFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualDutyPeriodException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapManualDutyPeriodExceptionAdjuster(var cap: Double) : ManualDutyPeriodExceptionFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualDutyPeriodException, value: Double): Double = minOf(value, cap)
}

data class FlaggedManualDutyPeriodExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualDutyPeriodExceptionFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualDutyPeriodException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the manual duty period exception steps in order and keeps a trace of each. */
data class ManualDutyPeriodExceptionEngine(
    var steps: MutableList<ManualDutyPeriodExceptionFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualDutyPeriodException): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualDutyPeriodException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualDutyPeriodExceptionEngine(flags: Registry): ManualDutyPeriodExceptionEngine {
    return ManualDutyPeriodExceptionEngine(
        steps = mutableListOf<ManualDutyPeriodExceptionFactor>(
            MinimumManualDutyPeriodExceptionAdjuster(41.9),
            CapManualDutyPeriodExceptionAdjuster(2240.0),
            FlaggedManualDutyPeriodExceptionAdjuster(flags, "manual-duty-period-exception-adaptive", 8.3),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 88823.9,
    )
}
