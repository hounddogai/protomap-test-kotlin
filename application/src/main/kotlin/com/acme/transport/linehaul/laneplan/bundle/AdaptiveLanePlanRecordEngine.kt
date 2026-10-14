package com.acme.transport.linehaul.laneplan.bundle

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive lane plan record calculation. */
interface AdaptiveLanePlanRecordFactor {
    fun describe(): String
    fun adjust(item: AdaptiveLanePlanRecord, value: Double): Double
}

data class CapAdaptiveLanePlanRecordAdjuster(var cap: Double) : AdaptiveLanePlanRecordFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveLanePlanRecord, value: Double): Double = minOf(value, cap)
}

data class LocaleAdaptiveLanePlanRecordAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveLanePlanRecordFactor {
    override fun describe(): String = "by locale"

    override fun adjust(item: AdaptiveLanePlanRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingAdaptiveLanePlanRecordAdjuster(var step: Double) : AdaptiveLanePlanRecordFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveLanePlanRecord, value: Double): Double = roundTo(value, step)
}

data class AdaptiveLanePlanRecordGraceDaysOffsetAdjuster(var weight: Double) : AdaptiveLanePlanRecordFactor {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: AdaptiveLanePlanRecord, value: Double): Double {
        return value + item.graceDays.toDouble() * weight
    }
}

data class TieredAdaptiveLanePlanRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveLanePlanRecordFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveLanePlanRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumAdaptiveLanePlanRecordAdjuster(var minimum: Double) : AdaptiveLanePlanRecordFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveLanePlanRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the adaptive lane plan record steps in order and keeps a trace of each. */
data class AdaptiveLanePlanRecordEngine(
    var steps: MutableList<AdaptiveLanePlanRecordFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AdaptiveLanePlanRecord): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveLanePlanRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAdaptiveLanePlanRecordEngine(): AdaptiveLanePlanRecordEngine {
    return AdaptiveLanePlanRecordEngine(
        steps = mutableListOf<AdaptiveLanePlanRecordFactor>(
            CapAdaptiveLanePlanRecordAdjuster(38487.0),
            LocaleAdaptiveLanePlanRecordAdjuster(mutableMapOf<String, Double>("retail" to 1.1)),
            RoundingAdaptiveLanePlanRecordAdjuster(0.05),
            AdaptiveLanePlanRecordGraceDaysOffsetAdjuster(1.93),
            TieredAdaptiveLanePlanRecordAdjuster(
                mutableListOf<Double>(2686.0, 3361.0, 5967.0, 8134.0),
                mutableListOf<Double>(0.94, 1.13, 1.11, 0.83),
            ),
            MinimumAdaptiveLanePlanRecordAdjuster(43.4),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 76504.0,
    )
}
