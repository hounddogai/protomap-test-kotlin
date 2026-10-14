package com.acme.transport.freightpay.dispute.metric

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the dispute profile calculation. */
interface DisputeProfileFactor {
    fun describe(): String
    fun adjust(item: DisputeProfile, value: Double): Double
}

data class CapDisputeProfileAdjuster(var cap: Double) : DisputeProfileFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DisputeProfile, value: Double): Double = minOf(value, cap)
}

data class PercentDisputeProfileAdjuster(var percent: Double) : DisputeProfileFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DisputeProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredDisputeProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DisputeProfileFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DisputeProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingDisputeProfileAdjuster(var step: Double) : DisputeProfileFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DisputeProfile, value: Double): Double = roundTo(value, step)
}

data class MinimumDisputeProfileAdjuster(var minimum: Double) : DisputeProfileFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DisputeProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the dispute profile steps in order and keeps a trace of each. */
data class DisputeProfileEngine(
    var steps: MutableList<DisputeProfileFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DisputeProfile): Double {
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

    fun evaluateAll(items: MutableList<DisputeProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDisputeProfileEngine(): DisputeProfileEngine {
    return DisputeProfileEngine(
        steps = mutableListOf<DisputeProfileFactor>(
            CapDisputeProfileAdjuster(27761.0),
            PercentDisputeProfileAdjuster(-23.3),
            TieredDisputeProfileAdjuster(
                mutableListOf<Double>(3350.0, 5082.0, 8204.0),
                mutableListOf<Double>(1.11, 1.18, 1.18),
            ),
            RoundingDisputeProfileAdjuster(1.0),
            MinimumDisputeProfileAdjuster(34.3),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 2497.3,
    )
}
