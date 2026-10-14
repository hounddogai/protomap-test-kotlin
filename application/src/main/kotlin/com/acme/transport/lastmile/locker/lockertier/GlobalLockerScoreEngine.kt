package com.acme.transport.lastmile.locker.lockertier

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global locker score calculation. */
interface GlobalLockerScoreStep {
    fun describe(): String
    fun adjust(item: GlobalLockerScore, value: Double): Double
}

data class PercentGlobalLockerScoreAdjuster(var percent: Double) : GlobalLockerScoreStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalLockerScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumGlobalLockerScoreAdjuster(var minimum: Double) : GlobalLockerScoreStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalLockerScore, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingGlobalLockerScoreAdjuster(var step: Double) : GlobalLockerScoreStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalLockerScore, value: Double): Double = roundTo(value, step)
}

data class GlobalLockerScoreLoadKgOffsetAdjuster(var weight: Double) : GlobalLockerScoreStep {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: GlobalLockerScore, value: Double): Double = value + item.loadKg * weight
}

data class CapGlobalLockerScoreAdjuster(var cap: Double) : GlobalLockerScoreStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalLockerScore, value: Double): Double = minOf(value, cap)
}

data class TieredGlobalLockerScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalLockerScoreStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalLockerScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the global locker score steps in order and keeps a trace of each. */
data class GlobalLockerScoreEngine(
    var steps: MutableList<GlobalLockerScoreStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: GlobalLockerScore): Double {
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

    fun evaluateAll(items: MutableList<GlobalLockerScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultGlobalLockerScoreEngine(): GlobalLockerScoreEngine {
    return GlobalLockerScoreEngine(
        steps = mutableListOf<GlobalLockerScoreStep>(
            PercentGlobalLockerScoreAdjuster(1.5),
            MinimumGlobalLockerScoreAdjuster(9.4),
            RoundingGlobalLockerScoreAdjuster(1.0),
            GlobalLockerScoreLoadKgOffsetAdjuster(1.36),
            CapGlobalLockerScoreAdjuster(3933.0),
            TieredGlobalLockerScoreAdjuster(
                mutableListOf<Double>(2166.0, 7635.0),
                mutableListOf<Double>(0.94, 0.86),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 57920.0,
    )
}
