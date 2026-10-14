package com.acme.transport.crewing.dutyperiod.summary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lenient duty period variance calculation. */
interface LenientDutyPeriodVarianceStep {
    fun describe(): String
    fun adjust(item: LenientDutyPeriodVariance, value: Double): Double
}

data class LenientDutyPeriodVarianceDistanceKmOffsetAdjuster(
    var weight: Double,
) : LenientDutyPeriodVarianceStep {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: LenientDutyPeriodVariance, value: Double): Double = value + item.distanceKm * weight
}

data class LocaleLenientDutyPeriodVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientDutyPeriodVarianceStep {
    override fun describe(): String = "by locale"

    override fun adjust(item: LenientDutyPeriodVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedLenientDutyPeriodVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientDutyPeriodVarianceStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientDutyPeriodVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapLenientDutyPeriodVarianceAdjuster(var cap: Double) : LenientDutyPeriodVarianceStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientDutyPeriodVariance, value: Double): Double = minOf(value, cap)
}

data class PercentLenientDutyPeriodVarianceAdjuster(var percent: Double) : LenientDutyPeriodVarianceStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientDutyPeriodVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the lenient duty period variance steps in order and keeps a trace of each. */
data class LenientDutyPeriodVarianceEngine(
    var steps: MutableList<LenientDutyPeriodVarianceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LenientDutyPeriodVariance): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientDutyPeriodVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLenientDutyPeriodVarianceEngine(flags: Registry): LenientDutyPeriodVarianceEngine {
    return LenientDutyPeriodVarianceEngine(
        steps = mutableListOf<LenientDutyPeriodVarianceStep>(
            LenientDutyPeriodVarianceDistanceKmOffsetAdjuster(0.93),
            LocaleLenientDutyPeriodVarianceAdjuster(
                mutableMapOf<String, Double>("retail" to 1.25, "east" to 1.05, "west" to 1.33),
            ),
            FlaggedLenientDutyPeriodVarianceAdjuster(flags, "lenient-duty-period-variance-default", 9.4),
            CapLenientDutyPeriodVarianceAdjuster(29140.0),
            PercentLenientDutyPeriodVarianceAdjuster(-8.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 94195.4,
    )
}
