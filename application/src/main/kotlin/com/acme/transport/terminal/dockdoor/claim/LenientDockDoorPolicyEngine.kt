package com.acme.transport.terminal.dockdoor.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lenient dock door policy calculation. */
interface LenientDockDoorPolicyFactor {
    fun describe(): String
    fun adjust(item: LenientDockDoorPolicy, value: Double): Double
}

data class RoundingLenientDockDoorPolicyAdjuster(var step: Double) : LenientDockDoorPolicyFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientDockDoorPolicy, value: Double): Double = roundTo(value, step)
}

data class LenientDockDoorPolicyWindowDaysOffsetAdjuster(var weight: Double) : LenientDockDoorPolicyFactor {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: LenientDockDoorPolicy, value: Double): Double {
        return value + item.windowDays.toDouble() * weight
    }
}

data class FlaggedLenientDockDoorPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientDockDoorPolicyFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientDockDoorPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumLenientDockDoorPolicyAdjuster(var minimum: Double) : LenientDockDoorPolicyFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientDockDoorPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredLenientDockDoorPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientDockDoorPolicyFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientDockDoorPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the lenient dock door policy steps in order and keeps a trace of each. */
data class LenientDockDoorPolicyEngine(
    var steps: MutableList<LenientDockDoorPolicyFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LenientDockDoorPolicy): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientDockDoorPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLenientDockDoorPolicyEngine(flags: Registry): LenientDockDoorPolicyEngine {
    return LenientDockDoorPolicyEngine(
        steps = mutableListOf<LenientDockDoorPolicyFactor>(
            RoundingLenientDockDoorPolicyAdjuster(0.05),
            LenientDockDoorPolicyWindowDaysOffsetAdjuster(0.36),
            FlaggedLenientDockDoorPolicyAdjuster(flags, "lenient-dock-door-policy-legacy", 0.6),
            MinimumLenientDockDoorPolicyAdjuster(9.6),
            TieredLenientDockDoorPolicyAdjuster(
                mutableListOf<Double>(1826.0, 5132.0),
                mutableListOf<Double>(1.12, 0.86),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 94790.5,
    )
}
