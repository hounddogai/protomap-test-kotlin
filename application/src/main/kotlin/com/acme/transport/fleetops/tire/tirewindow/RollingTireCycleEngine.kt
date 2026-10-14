package com.acme.transport.fleetops.tire.tirewindow

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling tire cycle calculation. */
interface RollingTireCycleStep {
    fun describe(): String
    fun adjust(item: RollingTireCycle, value: Double): Double
}

data class CapRollingTireCycleAdjuster(var cap: Double) : RollingTireCycleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingTireCycle, value: Double): Double = minOf(value, cap)
}

data class FlaggedRollingTireCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingTireCycleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingTireCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredRollingTireCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingTireCycleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingTireCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingRollingTireCycleAdjuster(var step: Double) : RollingTireCycleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingTireCycle, value: Double): Double = roundTo(value, step)
}

/** Runs the rolling tire cycle steps in order and keeps a trace of each. */
data class RollingTireCycleEngine(
    var steps: MutableList<RollingTireCycleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RollingTireCycle): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingTireCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRollingTireCycleEngine(flags: Registry): RollingTireCycleEngine {
    return RollingTireCycleEngine(
        steps = mutableListOf<RollingTireCycleStep>(
            CapRollingTireCycleAdjuster(14620.0),
            FlaggedRollingTireCycleAdjuster(flags, "rolling-tire-cycle-seasonal", 9.0),
            TieredRollingTireCycleAdjuster(
                mutableListOf<Double>(1189.0, 2312.0, 3497.0),
                mutableListOf<Double>(1.06, 0.91, 0.99),
            ),
            RoundingRollingTireCycleAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 69339.9,
    )
}
