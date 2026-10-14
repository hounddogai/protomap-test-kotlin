package com.acme.transport.crewing.swap.exception

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the seasonal swap exception calculation. */
interface SeasonalSwapExceptionAdjuster {
    fun describe(): String
    fun adjust(item: SeasonalSwapException, value: Double): Double
}

data class TieredSeasonalSwapExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalSwapExceptionAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalSwapException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SeasonalSwapExceptionFloorValueOffsetAdjuster(var weight: Double) : SeasonalSwapExceptionAdjuster {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: SeasonalSwapException, value: Double): Double = value + item.floorValue * weight
}

data class FlaggedSeasonalSwapExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalSwapExceptionAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalSwapException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapSeasonalSwapExceptionAdjuster(var cap: Double) : SeasonalSwapExceptionAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalSwapException, value: Double): Double = minOf(value, cap)
}

data class PercentSeasonalSwapExceptionAdjuster(var percent: Double) : SeasonalSwapExceptionAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalSwapException, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the seasonal swap exception steps in order and keeps a trace of each. */
data class SeasonalSwapExceptionEngine(
    var steps: MutableList<SeasonalSwapExceptionAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: SeasonalSwapException): Double {
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

    fun evaluateAll(items: MutableList<SeasonalSwapException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultSeasonalSwapExceptionEngine(flags: Registry): SeasonalSwapExceptionEngine {
    return SeasonalSwapExceptionEngine(
        steps = mutableListOf<SeasonalSwapExceptionAdjuster>(
            TieredSeasonalSwapExceptionAdjuster(
                mutableListOf<Double>(1628.0, 1745.0),
                mutableListOf<Double>(1.19, 1.11),
            ),
            SeasonalSwapExceptionFloorValueOffsetAdjuster(0.29),
            FlaggedSeasonalSwapExceptionAdjuster(flags, "seasonal-swap-exception-adaptive", 3.5),
            CapSeasonalSwapExceptionAdjuster(48456.0),
            PercentSeasonalSwapExceptionAdjuster(-6.9),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 61797.3,
    )
}
