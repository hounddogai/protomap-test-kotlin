package com.acme.transport.crewing.roster.variance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rolling roster cycle calculation. */
interface RollingRosterCycleModifier {
    fun describe(): String
    fun adjust(item: RollingRosterCycle, value: Double): Double
}

data class FlaggedRollingRosterCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingRosterCycleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingRosterCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentRollingRosterCycleAdjuster(var percent: Double) : RollingRosterCycleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingRosterCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredRollingRosterCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingRosterCycleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingRosterCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the rolling roster cycle steps in order and keeps a trace of each. */
data class RollingRosterCycleEngine(
    var steps: MutableList<RollingRosterCycleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RollingRosterCycle): Double {
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

    fun evaluateAll(items: MutableList<RollingRosterCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRollingRosterCycleEngine(flags: Registry): RollingRosterCycleEngine {
    return RollingRosterCycleEngine(
        steps = mutableListOf<RollingRosterCycleModifier>(
            FlaggedRollingRosterCycleAdjuster(flags, "rolling-roster-cycle-legacy", -7.8),
            PercentRollingRosterCycleAdjuster(-14.9),
            TieredRollingRosterCycleAdjuster(
                mutableListOf<Double>(2599.0, 9734.0),
                mutableListOf<Double>(0.96, 1.03),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 73755.9,
    )
}
