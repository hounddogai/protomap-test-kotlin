package com.acme.transport.crewing.dutyperiod.globalperiodsnapshot

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive duty period checkpoint calculation. */
interface AdaptiveDutyPeriodCheckpointModifier {
    fun describe(): String
    fun adjust(item: AdaptiveDutyPeriodCheckpoint, value: Double): Double
}

data class AdaptiveDutyPeriodCheckpointRetriesOffsetAdjuster(
    var weight: Double,
) : AdaptiveDutyPeriodCheckpointModifier {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: AdaptiveDutyPeriodCheckpoint, value: Double): Double {
        return value + item.retries.toDouble() * weight
    }
}

data class MinimumAdaptiveDutyPeriodCheckpointAdjuster(
    var minimum: Double,
) : AdaptiveDutyPeriodCheckpointModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveDutyPeriodCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingAdaptiveDutyPeriodCheckpointAdjuster(
    var step: Double,
) : AdaptiveDutyPeriodCheckpointModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveDutyPeriodCheckpoint, value: Double): Double = roundTo(value, step)
}

/** Runs the adaptive duty period checkpoint steps in order and keeps a trace of each. */
data class AdaptiveDutyPeriodCheckpointEngine(
    var steps: MutableList<AdaptiveDutyPeriodCheckpointModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AdaptiveDutyPeriodCheckpoint): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveDutyPeriodCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAdaptiveDutyPeriodCheckpointEngine(): AdaptiveDutyPeriodCheckpointEngine {
    return AdaptiveDutyPeriodCheckpointEngine(
        steps = mutableListOf<AdaptiveDutyPeriodCheckpointModifier>(
            AdaptiveDutyPeriodCheckpointRetriesOffsetAdjuster(1.14),
            MinimumAdaptiveDutyPeriodCheckpointAdjuster(36.5),
            RoundingAdaptiveDutyPeriodCheckpointAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 11681.4,
    )
}
