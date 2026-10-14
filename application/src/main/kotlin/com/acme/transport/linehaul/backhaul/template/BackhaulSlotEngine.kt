package com.acme.transport.linehaul.backhaul.template

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the backhaul slot calculation. */
interface BackhaulSlotAdjuster {
    fun describe(): String
    fun adjust(item: BackhaulSlot, value: Double): Double
}

data class MinimumBackhaulSlotAdjuster(var minimum: Double) : BackhaulSlotAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BackhaulSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingBackhaulSlotAdjuster(var step: Double) : BackhaulSlotAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BackhaulSlot, value: Double): Double = roundTo(value, step)
}

data class TieredBackhaulSlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BackhaulSlotAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BackhaulSlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedBackhaulSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BackhaulSlotAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BackhaulSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentBackhaulSlotAdjuster(var percent: Double) : BackhaulSlotAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BackhaulSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapBackhaulSlotAdjuster(var cap: Double) : BackhaulSlotAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BackhaulSlot, value: Double): Double = minOf(value, cap)
}

/** Runs the backhaul slot steps in order and keeps a trace of each. */
data class BackhaulSlotEngine(
    var steps: MutableList<BackhaulSlotAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BackhaulSlot): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BackhaulSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBackhaulSlotEngine(flags: Registry): BackhaulSlotEngine {
    return BackhaulSlotEngine(
        steps = mutableListOf<BackhaulSlotAdjuster>(
            MinimumBackhaulSlotAdjuster(11.9),
            RoundingBackhaulSlotAdjuster(1.0),
            TieredBackhaulSlotAdjuster(
                mutableListOf<Double>(933.0, 3678.0, 8446.0, 8506.0),
                mutableListOf<Double>(1.01, 1.09, 1.17, 0.93),
            ),
            FlaggedBackhaulSlotAdjuster(flags, "backhaul-slot-seasonal", -5.1),
            PercentBackhaulSlotAdjuster(-14.5),
            CapBackhaulSlotAdjuster(12027.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 65273.8,
    )
}
