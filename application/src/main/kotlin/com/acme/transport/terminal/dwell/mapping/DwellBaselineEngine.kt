package com.acme.transport.terminal.dwell.mapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the dwell baseline calculation. */
interface DwellBaselineFactor {
    fun describe(): String
    fun adjust(item: DwellBaseline, value: Double): Double
}

data class PercentDwellBaselineAdjuster(var percent: Double) : DwellBaselineFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DwellBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class DwellBaselineMinimumOffsetAdjuster(var weight: Double) : DwellBaselineFactor {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: DwellBaseline, value: Double): Double = value + item.minimum * weight
}

data class RoundingDwellBaselineAdjuster(var step: Double) : DwellBaselineFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DwellBaseline, value: Double): Double = roundTo(value, step)
}

data class FlaggedDwellBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DwellBaselineFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DwellBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapDwellBaselineAdjuster(var cap: Double) : DwellBaselineFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DwellBaseline, value: Double): Double = minOf(value, cap)
}

/** Runs the dwell baseline steps in order and keeps a trace of each. */
data class DwellBaselineEngine(
    var steps: MutableList<DwellBaselineFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DwellBaseline): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DwellBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDwellBaselineEngine(flags: Registry): DwellBaselineEngine {
    return DwellBaselineEngine(
        steps = mutableListOf<DwellBaselineFactor>(
            PercentDwellBaselineAdjuster(3.2),
            DwellBaselineMinimumOffsetAdjuster(1.52),
            RoundingDwellBaselineAdjuster(0.1),
            FlaggedDwellBaselineAdjuster(flags, "dwell-baseline-batched", 6.3),
            CapDwellBaselineAdjuster(49670.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 17642.5,
    )
}
