package com.acme.transport.lastmile.parcel.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the strict parcel cycle calculation. */
interface StrictParcelCycleModifier {
    fun describe(): String
    fun adjust(item: StrictParcelCycle, value: Double): Double
}

data class RoundingStrictParcelCycleAdjuster(var step: Double) : StrictParcelCycleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictParcelCycle, value: Double): Double = roundTo(value, step)
}

data class FlaggedStrictParcelCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictParcelCycleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictParcelCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NameStrictParcelCycleAdjuster(var factors: MutableMap<String, Double>) : StrictParcelCycleModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: StrictParcelCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentStrictParcelCycleAdjuster(var percent: Double) : StrictParcelCycleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictParcelCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapStrictParcelCycleAdjuster(var cap: Double) : StrictParcelCycleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictParcelCycle, value: Double): Double = minOf(value, cap)
}

/** Runs the strict parcel cycle steps in order and keeps a trace of each. */
data class StrictParcelCycleEngine(
    var steps: MutableList<StrictParcelCycleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: StrictParcelCycle): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictParcelCycleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<StrictParcelCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultStrictParcelCycleEngine(flags: Registry): StrictParcelCycleEngine {
    return StrictParcelCycleEngine(
        steps = mutableListOf<StrictParcelCycleModifier>(
            RoundingStrictParcelCycleAdjuster(0.5),
            FlaggedStrictParcelCycleAdjuster(flags, "strict-parcel-cycle-manual", -7.0),
            NameStrictParcelCycleAdjuster(mutableMapOf<String, Double>("east" to 0.76)),
            PercentStrictParcelCycleAdjuster(13.7),
            CapStrictParcelCycleAdjuster(30280.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 23087.1,
    )
}
