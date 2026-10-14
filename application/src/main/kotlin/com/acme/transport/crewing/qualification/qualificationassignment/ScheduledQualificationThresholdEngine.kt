package com.acme.transport.crewing.qualification.qualificationassignment

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the scheduled qualification threshold calculation. */
interface ScheduledQualificationThresholdStep {
    fun describe(): String
    fun adjust(item: ScheduledQualificationThreshold, value: Double): Double
}

data class MinimumScheduledQualificationThresholdAdjuster(
    var minimum: Double,
) : ScheduledQualificationThresholdStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledQualificationThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredScheduledQualificationThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledQualificationThresholdStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledQualificationThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingScheduledQualificationThresholdAdjuster(
    var step: Double,
) : ScheduledQualificationThresholdStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledQualificationThreshold, value: Double): Double = roundTo(value, step)
}

data class ScheduledQualificationThresholdVersionOffsetAdjuster(
    var weight: Double,
) : ScheduledQualificationThresholdStep {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: ScheduledQualificationThreshold, value: Double): Double {
        return value + item.version.toDouble() * weight
    }
}

data class CapScheduledQualificationThresholdAdjuster(var cap: Double) : ScheduledQualificationThresholdStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledQualificationThreshold, value: Double): Double = minOf(value, cap)
}

/** Runs the scheduled qualification threshold steps in order and keeps a trace of each. */
data class ScheduledQualificationThresholdEngine(
    var steps: MutableList<ScheduledQualificationThresholdStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScheduledQualificationThreshold): Double {
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

    fun evaluateAll(items: MutableList<ScheduledQualificationThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScheduledQualificationThresholdEngine(): ScheduledQualificationThresholdEngine {
    return ScheduledQualificationThresholdEngine(
        steps = mutableListOf<ScheduledQualificationThresholdStep>(
            MinimumScheduledQualificationThresholdAdjuster(5.2),
            TieredScheduledQualificationThresholdAdjuster(
                mutableListOf<Double>(705.0, 6155.0, 7834.0, 8614.0),
                mutableListOf<Double>(1.0, 1.11, 1.19, 1.05),
            ),
            RoundingScheduledQualificationThresholdAdjuster(1.0),
            ScheduledQualificationThresholdVersionOffsetAdjuster(0.56),
            CapScheduledQualificationThresholdAdjuster(2833.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 33201.8,
    )
}
