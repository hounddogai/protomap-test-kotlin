package com.acme.transport.linehaul.capacity.preference

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the primary capacity window calculation. */
interface PrimaryCapacityWindowStep {
    fun describe(): String
    fun adjust(item: PrimaryCapacityWindow, value: Double): Double
}

data class RoundingPrimaryCapacityWindowAdjuster(var step: Double) : PrimaryCapacityWindowStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryCapacityWindow, value: Double): Double = roundTo(value, step)
}

data class PrimaryCapacityWindowRetriesOffsetAdjuster(var weight: Double) : PrimaryCapacityWindowStep {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: PrimaryCapacityWindow, value: Double): Double = value + item.retries.toDouble() * weight
}

data class MinimumPrimaryCapacityWindowAdjuster(var minimum: Double) : PrimaryCapacityWindowStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryCapacityWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapPrimaryCapacityWindowAdjuster(var cap: Double) : PrimaryCapacityWindowStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryCapacityWindow, value: Double): Double = minOf(value, cap)
}

data class TieredPrimaryCapacityWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryCapacityWindowStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryCapacityWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the primary capacity window steps in order and keeps a trace of each. */
data class PrimaryCapacityWindowEngine(
    var steps: MutableList<PrimaryCapacityWindowStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PrimaryCapacityWindow): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryCapacityWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPrimaryCapacityWindowEngine(): PrimaryCapacityWindowEngine {
    return PrimaryCapacityWindowEngine(
        steps = mutableListOf<PrimaryCapacityWindowStep>(
            RoundingPrimaryCapacityWindowAdjuster(1.0),
            PrimaryCapacityWindowRetriesOffsetAdjuster(1.78),
            MinimumPrimaryCapacityWindowAdjuster(26.1),
            CapPrimaryCapacityWindowAdjuster(30258.0),
            TieredPrimaryCapacityWindowAdjuster(
                mutableListOf<Double>(315.0, 7994.0),
                mutableListOf<Double>(1.16, 0.84),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 50787.6,
    )
}
