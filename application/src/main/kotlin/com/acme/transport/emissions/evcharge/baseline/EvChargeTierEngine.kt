package com.acme.transport.emissions.evcharge.baseline

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the ev charge tier calculation. */
interface EvChargeTierAdjuster {
    fun describe(): String
    fun adjust(item: EvChargeTier, value: Double): Double
}

data class EvChargeTierGraceDaysOffsetAdjuster(var weight: Double) : EvChargeTierAdjuster {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: EvChargeTier, value: Double): Double = value + item.graceDays.toDouble() * weight
}

data class RoundingEvChargeTierAdjuster(var step: Double) : EvChargeTierAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: EvChargeTier, value: Double): Double = roundTo(value, step)
}

data class TieredEvChargeTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : EvChargeTierAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: EvChargeTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the ev charge tier steps in order and keeps a trace of each. */
data class EvChargeTierEngine(
    var steps: MutableList<EvChargeTierAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: EvChargeTier): Double {
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

    fun evaluateAll(items: MutableList<EvChargeTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultEvChargeTierEngine(): EvChargeTierEngine {
    return EvChargeTierEngine(
        steps = mutableListOf<EvChargeTierAdjuster>(
            EvChargeTierGraceDaysOffsetAdjuster(1.53),
            RoundingEvChargeTierAdjuster(5.0),
            TieredEvChargeTierAdjuster(
                mutableListOf<Double>(1647.0, 5353.0, 6366.0),
                mutableListOf<Double>(0.92, 1.18, 0.97),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 51025.1,
    )
}
