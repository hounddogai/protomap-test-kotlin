package com.acme.transport.linehaul.backhaul.reservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the backhaul cycle calculation. */
interface BackhaulCycleAdjuster {
    fun describe(): String
    fun adjust(item: BackhaulCycle, value: Double): Double
}

data class TieredBackhaulCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BackhaulCycleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BackhaulCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedBackhaulCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BackhaulCycleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BackhaulCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingBackhaulCycleAdjuster(var step: Double) : BackhaulCycleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BackhaulCycle, value: Double): Double = roundTo(value, step)
}

data class BackhaulCycleCostOffsetAdjuster(var weight: Double) : BackhaulCycleAdjuster {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: BackhaulCycle, value: Double): Double = value + item.cost * weight
}

/** Runs the backhaul cycle steps in order and keeps a trace of each. */
data class BackhaulCycleEngine(
    var steps: MutableList<BackhaulCycleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BackhaulCycle): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BackhaulCycleAdjuster = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BackhaulCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBackhaulCycleEngine(flags: Registry): BackhaulCycleEngine {
    return BackhaulCycleEngine(
        steps = mutableListOf<BackhaulCycleAdjuster>(
            TieredBackhaulCycleAdjuster(
                mutableListOf<Double>(2738.0, 4051.0, 4112.0, 5035.0),
                mutableListOf<Double>(0.86, 1.04, 0.85, 1.19),
            ),
            FlaggedBackhaulCycleAdjuster(flags, "backhaul-cycle-weighted", -6.7),
            RoundingBackhaulCycleAdjuster(0.1),
            BackhaulCycleCostOffsetAdjuster(1.13),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 26416.5,
    )
}
