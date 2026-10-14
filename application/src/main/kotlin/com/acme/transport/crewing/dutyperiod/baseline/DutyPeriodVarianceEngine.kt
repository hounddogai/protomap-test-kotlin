package com.acme.transport.crewing.dutyperiod.baseline

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the duty period variance calculation. */
interface DutyPeriodVarianceFactor {
    fun describe(): String
    fun adjust(item: DutyPeriodVariance, value: Double): Double
}

data class DutyPeriodVarianceVersionOffsetAdjuster(var weight: Double) : DutyPeriodVarianceFactor {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: DutyPeriodVariance, value: Double): Double = value + item.version.toDouble() * weight
}

data class CapDutyPeriodVarianceAdjuster(var cap: Double) : DutyPeriodVarianceFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DutyPeriodVariance, value: Double): Double = minOf(value, cap)
}

data class RoundingDutyPeriodVarianceAdjuster(var step: Double) : DutyPeriodVarianceFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DutyPeriodVariance, value: Double): Double = roundTo(value, step)
}

data class PercentDutyPeriodVarianceAdjuster(var percent: Double) : DutyPeriodVarianceFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DutyPeriodVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class NameDutyPeriodVarianceAdjuster(var factors: MutableMap<String, Double>) : DutyPeriodVarianceFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: DutyPeriodVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the duty period variance steps in order and keeps a trace of each. */
data class DutyPeriodVarianceEngine(
    var steps: MutableList<DutyPeriodVarianceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DutyPeriodVariance): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DutyPeriodVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDutyPeriodVarianceEngine(): DutyPeriodVarianceEngine {
    return DutyPeriodVarianceEngine(
        steps = mutableListOf<DutyPeriodVarianceFactor>(
            DutyPeriodVarianceVersionOffsetAdjuster(1.17),
            CapDutyPeriodVarianceAdjuster(26088.0),
            RoundingDutyPeriodVarianceAdjuster(0.05),
            PercentDutyPeriodVarianceAdjuster(1.4),
            NameDutyPeriodVarianceAdjuster(mutableMapOf<String, Double>("south" to 1.08)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 49822.5,
    )
}
