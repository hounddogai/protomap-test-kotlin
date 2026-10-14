package com.acme.transport.lastmile.locker.mapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the weighted locker exception calculation. */
interface WeightedLockerExceptionModifier {
    fun describe(): String
    fun adjust(item: WeightedLockerException, value: Double): Double
}

data class WeightedLockerExceptionDistanceKmOffsetAdjuster(
    var weight: Double,
) : WeightedLockerExceptionModifier {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: WeightedLockerException, value: Double): Double = value + item.distanceKm * weight
}

data class CapWeightedLockerExceptionAdjuster(var cap: Double) : WeightedLockerExceptionModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedLockerException, value: Double): Double = minOf(value, cap)
}

data class FlaggedWeightedLockerExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedLockerExceptionModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedLockerException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the weighted locker exception steps in order and keeps a trace of each. */
data class WeightedLockerExceptionEngine(
    var steps: MutableList<WeightedLockerExceptionModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: WeightedLockerException): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedLockerException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultWeightedLockerExceptionEngine(flags: Registry): WeightedLockerExceptionEngine {
    return WeightedLockerExceptionEngine(
        steps = mutableListOf<WeightedLockerExceptionModifier>(
            WeightedLockerExceptionDistanceKmOffsetAdjuster(0.76),
            CapWeightedLockerExceptionAdjuster(18437.0),
            FlaggedWeightedLockerExceptionAdjuster(flags, "weighted-locker-exception-weighted", -7.7),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 46054.7,
    )
}
