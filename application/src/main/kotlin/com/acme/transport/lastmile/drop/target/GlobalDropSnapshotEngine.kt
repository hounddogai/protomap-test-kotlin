package com.acme.transport.lastmile.drop.target

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global drop snapshot calculation. */
interface GlobalDropSnapshotFactor {
    fun describe(): String
    fun adjust(item: GlobalDropSnapshot, value: Double): Double
}

data class RoundingGlobalDropSnapshotAdjuster(var step: Double) : GlobalDropSnapshotFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalDropSnapshot, value: Double): Double = roundTo(value, step)
}

data class TieredGlobalDropSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalDropSnapshotFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalDropSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class GlobalDropSnapshotFuelLitersOffsetAdjuster(var weight: Double) : GlobalDropSnapshotFactor {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: GlobalDropSnapshot, value: Double): Double = value + item.fuelLiters * weight
}

/** Runs the global drop snapshot steps in order and keeps a trace of each. */
data class GlobalDropSnapshotEngine(
    var steps: MutableList<GlobalDropSnapshotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalDropSnapshot): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalDropSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalDropSnapshotEngine(): GlobalDropSnapshotEngine {
    return GlobalDropSnapshotEngine(
        steps = mutableListOf<GlobalDropSnapshotFactor>(
            RoundingGlobalDropSnapshotAdjuster(5.0),
            TieredGlobalDropSnapshotAdjuster(
                mutableListOf<Double>(2099.0, 3168.0, 5015.0, 7690.0),
                mutableListOf<Double>(0.8, 0.83, 1.11, 1.08),
            ),
            GlobalDropSnapshotFuelLitersOffsetAdjuster(1.95),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 58538.9,
    )
}
