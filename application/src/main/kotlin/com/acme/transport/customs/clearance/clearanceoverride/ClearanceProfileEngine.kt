package com.acme.transport.customs.clearance.clearanceoverride

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the clearance profile calculation. */
interface ClearanceProfileAdjuster {
    fun describe(): String
    fun adjust(item: ClearanceProfile, value: Double): Double
}

data class RoundingClearanceProfileAdjuster(var step: Double) : ClearanceProfileAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ClearanceProfile, value: Double): Double = roundTo(value, step)
}

data class ClearanceProfileDurationMinutesOffsetAdjuster(var weight: Double) : ClearanceProfileAdjuster {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: ClearanceProfile, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

data class ChannelClearanceProfileAdjuster(var factors: MutableMap<String, Double>) : ClearanceProfileAdjuster {
    override fun describe(): String = "by channel"

    override fun adjust(item: ClearanceProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the clearance profile steps in order and keeps a trace of each. */
data class ClearanceProfileEngine(
    var steps: MutableList<ClearanceProfileAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ClearanceProfile): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ClearanceProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultClearanceProfileEngine(): ClearanceProfileEngine {
    return ClearanceProfileEngine(
        steps = mutableListOf<ClearanceProfileAdjuster>(
            RoundingClearanceProfileAdjuster(1.0),
            ClearanceProfileDurationMinutesOffsetAdjuster(0.38),
            ChannelClearanceProfileAdjuster(mutableMapOf<String, Double>("south" to 1.16)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 81803.9,
    )
}
