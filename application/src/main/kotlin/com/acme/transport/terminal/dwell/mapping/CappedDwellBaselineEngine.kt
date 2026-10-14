package com.acme.transport.terminal.dwell.mapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped dwell baseline calculation. */
interface CappedDwellBaselineFactor {
    fun describe(): String
    fun adjust(item: CappedDwellBaseline, value: Double): Double
}

data class PercentCappedDwellBaselineAdjuster(var percent: Double) : CappedDwellBaselineFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedDwellBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumCappedDwellBaselineAdjuster(var minimum: Double) : CappedDwellBaselineFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedDwellBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingCappedDwellBaselineAdjuster(var step: Double) : CappedDwellBaselineFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedDwellBaseline, value: Double): Double = roundTo(value, step)
}

data class CapCappedDwellBaselineAdjuster(var cap: Double) : CappedDwellBaselineFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedDwellBaseline, value: Double): Double = minOf(value, cap)
}

data class CappedDwellBaselineLoadKgOffsetAdjuster(var weight: Double) : CappedDwellBaselineFactor {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: CappedDwellBaseline, value: Double): Double = value + item.loadKg * weight
}

data class FlaggedCappedDwellBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedDwellBaselineFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedDwellBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the capped dwell baseline steps in order and keeps a trace of each. */
data class CappedDwellBaselineEngine(
    var steps: MutableList<CappedDwellBaselineFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CappedDwellBaseline): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedDwellBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCappedDwellBaselineEngine(flags: Registry): CappedDwellBaselineEngine {
    return CappedDwellBaselineEngine(
        steps = mutableListOf<CappedDwellBaselineFactor>(
            PercentCappedDwellBaselineAdjuster(12.0),
            MinimumCappedDwellBaselineAdjuster(6.2),
            RoundingCappedDwellBaselineAdjuster(0.1),
            CapCappedDwellBaselineAdjuster(2193.0),
            CappedDwellBaselineLoadKgOffsetAdjuster(0.72),
            FlaggedCappedDwellBaselineAdjuster(flags, "capped-dwell-baseline-batched", 0.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 24923.6,
    )
}
