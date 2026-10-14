package com.acme.transport.crewing.swap.exception

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the strict swap variance calculation. */
interface StrictSwapVarianceStep {
    fun describe(): String
    fun adjust(item: StrictSwapVariance, value: Double): Double
}

data class CapStrictSwapVarianceAdjuster(var cap: Double) : StrictSwapVarianceStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictSwapVariance, value: Double): Double = minOf(value, cap)
}

data class FlaggedStrictSwapVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictSwapVarianceStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictSwapVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class UnitStrictSwapVarianceAdjuster(var factors: MutableMap<String, Double>) : StrictSwapVarianceStep {
    override fun describe(): String = "by unit"

    override fun adjust(item: StrictSwapVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentStrictSwapVarianceAdjuster(var percent: Double) : StrictSwapVarianceStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictSwapVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingStrictSwapVarianceAdjuster(var step: Double) : StrictSwapVarianceStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictSwapVariance, value: Double): Double = roundTo(value, step)
}

data class StrictSwapVarianceMarginOffsetAdjuster(var weight: Double) : StrictSwapVarianceStep {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: StrictSwapVariance, value: Double): Double = value + item.margin * weight
}

/** Runs the strict swap variance steps in order and keeps a trace of each. */
data class StrictSwapVarianceEngine(
    var steps: MutableList<StrictSwapVarianceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: StrictSwapVariance): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictSwapVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultStrictSwapVarianceEngine(flags: Registry): StrictSwapVarianceEngine {
    return StrictSwapVarianceEngine(
        steps = mutableListOf<StrictSwapVarianceStep>(
            CapStrictSwapVarianceAdjuster(15689.0),
            FlaggedStrictSwapVarianceAdjuster(flags, "strict-swap-variance-rolling", 3.5),
            UnitStrictSwapVarianceAdjuster(mutableMapOf<String, Double>("central" to 1.28, "retail" to 1.25)),
            PercentStrictSwapVarianceAdjuster(-23.1),
            RoundingStrictSwapVarianceAdjuster(0.05),
            StrictSwapVarianceMarginOffsetAdjuster(0.84),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 27900.2,
    )
}
