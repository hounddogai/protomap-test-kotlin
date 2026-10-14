package com.acme.transport.linehaul.backhaul.overrideitem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the regional backhaul baseline calculation. */
interface RegionalBackhaulBaselineStep {
    fun describe(): String
    fun adjust(item: RegionalBackhaulBaseline, value: Double): Double
}

data class RegionalBackhaulBaselineBatchSizeOffsetAdjuster(var weight: Double) : RegionalBackhaulBaselineStep {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: RegionalBackhaulBaseline, value: Double): Double {
        return value + item.batchSize.toDouble() * weight
    }
}

data class TieredRegionalBackhaulBaselineAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalBackhaulBaselineStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalBackhaulBaseline, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingRegionalBackhaulBaselineAdjuster(var step: Double) : RegionalBackhaulBaselineStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalBackhaulBaseline, value: Double): Double = roundTo(value, step)
}

data class FlaggedRegionalBackhaulBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalBackhaulBaselineStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalBackhaulBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the regional backhaul baseline steps in order and keeps a trace of each. */
data class RegionalBackhaulBaselineEngine(
    var steps: MutableList<RegionalBackhaulBaselineStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalBackhaulBaseline): Double {
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

    fun evaluateAll(items: MutableList<RegionalBackhaulBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalBackhaulBaselineEngine(flags: Registry): RegionalBackhaulBaselineEngine {
    return RegionalBackhaulBaselineEngine(
        steps = mutableListOf<RegionalBackhaulBaselineStep>(
            RegionalBackhaulBaselineBatchSizeOffsetAdjuster(1.53),
            TieredRegionalBackhaulBaselineAdjuster(
                mutableListOf<Double>(231.0, 4545.0, 8163.0),
                mutableListOf<Double>(1.0, 1.13, 1.05),
            ),
            RoundingRegionalBackhaulBaselineAdjuster(0.5),
            FlaggedRegionalBackhaulBaselineAdjuster(flags, "regional-backhaul-baseline-global", -4.7),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 96614.5,
    )
}
