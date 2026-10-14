package com.acme.transport.lastmile.redelivery.checkpoint

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the redelivery manifest calculation. */
interface RedeliveryManifestAdjuster {
    fun describe(): String
    fun adjust(item: RedeliveryManifest, value: Double): Double
}

data class RoundingRedeliveryManifestAdjuster(var step: Double) : RedeliveryManifestAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RedeliveryManifest, value: Double): Double = roundTo(value, step)
}

data class CapRedeliveryManifestAdjuster(var cap: Double) : RedeliveryManifestAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RedeliveryManifest, value: Double): Double = minOf(value, cap)
}

data class CategoryRedeliveryManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : RedeliveryManifestAdjuster {
    override fun describe(): String = "by category"

    override fun adjust(item: RedeliveryManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RedeliveryManifestFuelLitersOffsetAdjuster(var weight: Double) : RedeliveryManifestAdjuster {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: RedeliveryManifest, value: Double): Double = value + item.fuelLiters * weight
}

data class MinimumRedeliveryManifestAdjuster(var minimum: Double) : RedeliveryManifestAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RedeliveryManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the redelivery manifest steps in order and keeps a trace of each. */
data class RedeliveryManifestEngine(
    var steps: MutableList<RedeliveryManifestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RedeliveryManifest): Double {
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

    fun evaluateAll(items: MutableList<RedeliveryManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRedeliveryManifestEngine(): RedeliveryManifestEngine {
    return RedeliveryManifestEngine(
        steps = mutableListOf<RedeliveryManifestAdjuster>(
            RoundingRedeliveryManifestAdjuster(1.0),
            CapRedeliveryManifestAdjuster(24438.0),
            CategoryRedeliveryManifestAdjuster(mutableMapOf<String, Double>("south" to 1.19)),
            RedeliveryManifestFuelLitersOffsetAdjuster(0.9),
            MinimumRedeliveryManifestAdjuster(15.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 34738.6,
    )
}
