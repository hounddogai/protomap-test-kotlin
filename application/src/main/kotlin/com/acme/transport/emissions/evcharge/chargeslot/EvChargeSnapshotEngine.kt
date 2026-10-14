package com.acme.transport.emissions.evcharge.chargeslot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the ev charge snapshot calculation. */
interface EvChargeSnapshotStep {
    fun describe(): String
    fun adjust(item: EvChargeSnapshot, value: Double): Double
}

data class EvChargeSnapshotFuelLitersOffsetAdjuster(var weight: Double) : EvChargeSnapshotStep {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: EvChargeSnapshot, value: Double): Double = value + item.fuelLiters * weight
}

data class FlaggedEvChargeSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : EvChargeSnapshotStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: EvChargeSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentEvChargeSnapshotAdjuster(var percent: Double) : EvChargeSnapshotStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: EvChargeSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumEvChargeSnapshotAdjuster(var minimum: Double) : EvChargeSnapshotStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EvChargeSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredEvChargeSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : EvChargeSnapshotStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: EvChargeSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingEvChargeSnapshotAdjuster(var step: Double) : EvChargeSnapshotStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: EvChargeSnapshot, value: Double): Double = roundTo(value, step)
}

/** Runs the ev charge snapshot steps in order and keeps a trace of each. */
data class EvChargeSnapshotEngine(
    var steps: MutableList<EvChargeSnapshotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: EvChargeSnapshot): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<EvChargeSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultEvChargeSnapshotEngine(flags: Registry): EvChargeSnapshotEngine {
    return EvChargeSnapshotEngine(
        steps = mutableListOf<EvChargeSnapshotStep>(
            EvChargeSnapshotFuelLitersOffsetAdjuster(1.21),
            FlaggedEvChargeSnapshotAdjuster(flags, "ev-charge-snapshot-fallback", -7.9),
            PercentEvChargeSnapshotAdjuster(15.3),
            MinimumEvChargeSnapshotAdjuster(8.4),
            TieredEvChargeSnapshotAdjuster(
                mutableListOf<Double>(3837.0, 6108.0),
                mutableListOf<Double>(1.16, 0.97),
            ),
            RoundingEvChargeSnapshotAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 28088.5,
    )
}
