package com.acme.transport.terminal.dwell.dwellcycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling dwell cycle calculation. */
interface RollingDwellCycleFactor {
    fun describe(): String
    fun adjust(item: RollingDwellCycle, value: Double): Double
}

data class TitleRollingDwellCycleAdjuster(var factors: MutableMap<String, Double>) : RollingDwellCycleFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: RollingDwellCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentRollingDwellCycleAdjuster(var percent: Double) : RollingDwellCycleFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingDwellCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedRollingDwellCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingDwellCycleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingDwellCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredRollingDwellCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingDwellCycleFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingDwellCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumRollingDwellCycleAdjuster(var minimum: Double) : RollingDwellCycleFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingDwellCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingRollingDwellCycleAdjuster(var step: Double) : RollingDwellCycleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingDwellCycle, value: Double): Double = roundTo(value, step)
}

/** Runs the rolling dwell cycle steps in order and keeps a trace of each. */
data class RollingDwellCycleEngine(
    var steps: MutableList<RollingDwellCycleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingDwellCycle): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingDwellCycleFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RollingDwellCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingDwellCycleEngine(flags: Registry): RollingDwellCycleEngine {
    return RollingDwellCycleEngine(
        steps = mutableListOf<RollingDwellCycleFactor>(
            TitleRollingDwellCycleAdjuster(mutableMapOf<String, Double>("west" to 1.14)),
            PercentRollingDwellCycleAdjuster(4.5),
            FlaggedRollingDwellCycleAdjuster(flags, "rolling-dwell-cycle-batched", 0.0),
            TieredRollingDwellCycleAdjuster(
                mutableListOf<Double>(989.0, 3785.0, 6436.0, 9646.0),
                mutableListOf<Double>(1.19, 0.87, 0.9, 0.81),
            ),
            MinimumRollingDwellCycleAdjuster(19.8),
            RoundingRollingDwellCycleAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 44770.1,
    )
}
