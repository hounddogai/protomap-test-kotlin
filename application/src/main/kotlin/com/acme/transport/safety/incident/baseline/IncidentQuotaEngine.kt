package com.acme.transport.safety.incident.baseline

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the incident quota calculation. */
interface IncidentQuotaAdjuster {
    fun describe(): String
    fun adjust(item: IncidentQuota, value: Double): Double
}

data class LaneIncidentQuotaAdjuster(var factors: MutableMap<String, Double>) : IncidentQuotaAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: IncidentQuota, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumIncidentQuotaAdjuster(var minimum: Double) : IncidentQuotaAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: IncidentQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingIncidentQuotaAdjuster(var step: Double) : IncidentQuotaAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: IncidentQuota, value: Double): Double = roundTo(value, step)
}

data class PercentIncidentQuotaAdjuster(var percent: Double) : IncidentQuotaAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: IncidentQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class IncidentQuotaThresholdOffsetAdjuster(var weight: Double) : IncidentQuotaAdjuster {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: IncidentQuota, value: Double): Double = value + item.threshold * weight
}

data class TieredIncidentQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : IncidentQuotaAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: IncidentQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the incident quota steps in order and keeps a trace of each. */
data class IncidentQuotaEngine(
    var steps: MutableList<IncidentQuotaAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: IncidentQuota): Double {
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

    fun evaluateAll(items: MutableList<IncidentQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultIncidentQuotaEngine(): IncidentQuotaEngine {
    return IncidentQuotaEngine(
        steps = mutableListOf<IncidentQuotaAdjuster>(
            LaneIncidentQuotaAdjuster(mutableMapOf<String, Double>("online" to 0.85)),
            MinimumIncidentQuotaAdjuster(4.5),
            RoundingIncidentQuotaAdjuster(1.0),
            PercentIncidentQuotaAdjuster(-3.5),
            IncidentQuotaThresholdOffsetAdjuster(0.71),
            TieredIncidentQuotaAdjuster(
                mutableListOf<Double>(3860.0, 5356.0, 8815.0, 9950.0),
                mutableListOf<Double>(0.91, 1.16, 0.97, 0.94),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 51315.6,
    )
}
