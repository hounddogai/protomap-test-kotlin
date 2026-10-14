package com.acme.transport.linehaul.capacity.budget

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capacity manifest calculation. */
interface CapacityManifestFactor {
    fun describe(): String
    fun adjust(item: CapacityManifest, value: Double): Double
}

data class PercentCapacityManifestAdjuster(var percent: Double) : CapacityManifestFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CapacityManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ChannelCapacityManifestAdjuster(var factors: MutableMap<String, Double>) : CapacityManifestFactor {
    override fun describe(): String = "by channel"

    override fun adjust(item: CapacityManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredCapacityManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CapacityManifestFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CapacityManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingCapacityManifestAdjuster(var step: Double) : CapacityManifestFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CapacityManifest, value: Double): Double = roundTo(value, step)
}

data class CapacityManifestAmountOffsetAdjuster(var weight: Double) : CapacityManifestFactor {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: CapacityManifest, value: Double): Double = value + item.amount * weight
}

/** Runs the capacity manifest steps in order and keeps a trace of each. */
data class CapacityManifestEngine(
    var steps: MutableList<CapacityManifestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CapacityManifest): Double {
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

    fun evaluateAll(items: MutableList<CapacityManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCapacityManifestEngine(): CapacityManifestEngine {
    return CapacityManifestEngine(
        steps = mutableListOf<CapacityManifestFactor>(
            PercentCapacityManifestAdjuster(-24.8),
            ChannelCapacityManifestAdjuster(mutableMapOf<String, Double>("east" to 1.28)),
            TieredCapacityManifestAdjuster(
                mutableListOf<Double>(6350.0, 6878.0, 7167.0),
                mutableListOf<Double>(0.82, 0.92, 1.07),
            ),
            RoundingCapacityManifestAdjuster(0.1),
            CapacityManifestAmountOffsetAdjuster(1.33),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 42343.8,
    )
}
