package com.acme.transport.crewing.dutyperiod.summary

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the duty period exception calculation. */
interface DutyPeriodExceptionAdjuster {
    fun describe(): String
    fun adjust(item: DutyPeriodException, value: Double): Double
}

data class SourceDutyPeriodExceptionAdjuster(
    var factors: MutableMap<String, Double>,
) : DutyPeriodExceptionAdjuster {
    override fun describe(): String = "by source"

    override fun adjust(item: DutyPeriodException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentDutyPeriodExceptionAdjuster(var percent: Double) : DutyPeriodExceptionAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DutyPeriodException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class DutyPeriodExceptionGraceDaysOffsetAdjuster(var weight: Double) : DutyPeriodExceptionAdjuster {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: DutyPeriodException, value: Double): Double = value + item.graceDays.toDouble() * weight
}

data class CapDutyPeriodExceptionAdjuster(var cap: Double) : DutyPeriodExceptionAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DutyPeriodException, value: Double): Double = minOf(value, cap)
}

/** Runs the duty period exception steps in order and keeps a trace of each. */
data class DutyPeriodExceptionEngine(
    var steps: MutableList<DutyPeriodExceptionAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DutyPeriodException): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DutyPeriodException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDutyPeriodExceptionEngine(): DutyPeriodExceptionEngine {
    return DutyPeriodExceptionEngine(
        steps = mutableListOf<DutyPeriodExceptionAdjuster>(
            SourceDutyPeriodExceptionAdjuster(
                mutableMapOf<String, Double>("central" to 1.08, "east" to 1.27, "online" to 1.32),
            ),
            PercentDutyPeriodExceptionAdjuster(-8.9),
            DutyPeriodExceptionGraceDaysOffsetAdjuster(1.28),
            CapDutyPeriodExceptionAdjuster(13810.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 99503.2,
    )
}
