package com.acme.transport.customs.hold.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the hold cycle calculation. */
interface HoldCycleAdjuster {
    fun describe(): String
    fun adjust(item: HoldCycle, value: Double): Double
}

data class MinimumHoldCycleAdjuster(var minimum: Double) : HoldCycleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: HoldCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapHoldCycleAdjuster(var cap: Double) : HoldCycleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: HoldCycle, value: Double): Double = minOf(value, cap)
}

data class TieredHoldCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : HoldCycleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: HoldCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedHoldCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : HoldCycleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: HoldCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the hold cycle steps in order and keeps a trace of each. */
data class HoldCycleEngine(
    var steps: MutableList<HoldCycleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: HoldCycle): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<HoldCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultHoldCycleEngine(flags: Registry): HoldCycleEngine {
    return HoldCycleEngine(
        steps = mutableListOf<HoldCycleAdjuster>(
            MinimumHoldCycleAdjuster(40.9),
            CapHoldCycleAdjuster(615.0),
            TieredHoldCycleAdjuster(
                mutableListOf<Double>(483.0, 902.0, 1138.0, 8059.0),
                mutableListOf<Double>(0.88, 1.17, 0.87, 1.13),
            ),
            FlaggedHoldCycleAdjuster(flags, "hold-cycle-scheduled", 8.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 59500.3,
    )
}
