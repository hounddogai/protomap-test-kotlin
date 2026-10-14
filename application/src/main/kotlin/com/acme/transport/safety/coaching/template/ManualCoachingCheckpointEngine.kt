package com.acme.transport.safety.coaching.template

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the manual coaching checkpoint calculation. */
interface ManualCoachingCheckpointModifier {
    fun describe(): String
    fun adjust(item: ManualCoachingCheckpoint, value: Double): Double
}

data class MinimumManualCoachingCheckpointAdjuster(var minimum: Double) : ManualCoachingCheckpointModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualCoachingCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingManualCoachingCheckpointAdjuster(var step: Double) : ManualCoachingCheckpointModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualCoachingCheckpoint, value: Double): Double = roundTo(value, step)
}

data class TieredManualCoachingCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualCoachingCheckpointModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualCoachingCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentManualCoachingCheckpointAdjuster(var percent: Double) : ManualCoachingCheckpointModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualCoachingCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CountryManualCoachingCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualCoachingCheckpointModifier {
    override fun describe(): String = "by country"

    override fun adjust(item: ManualCoachingCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the manual coaching checkpoint steps in order and keeps a trace of each. */
data class ManualCoachingCheckpointEngine(
    var steps: MutableList<ManualCoachingCheckpointModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualCoachingCheckpoint): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualCoachingCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualCoachingCheckpointEngine(): ManualCoachingCheckpointEngine {
    return ManualCoachingCheckpointEngine(
        steps = mutableListOf<ManualCoachingCheckpointModifier>(
            MinimumManualCoachingCheckpointAdjuster(40.6),
            RoundingManualCoachingCheckpointAdjuster(0.5),
            TieredManualCoachingCheckpointAdjuster(
                mutableListOf<Double>(3145.0, 4757.0, 6297.0),
                mutableListOf<Double>(1.12, 0.82, 0.99),
            ),
            PercentManualCoachingCheckpointAdjuster(16.9),
            CountryManualCoachingCheckpointAdjuster(
                mutableMapOf<String, Double>("retail" to 0.99, "west" to 1.04),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 90226.1,
    )
}
