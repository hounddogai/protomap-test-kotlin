package com.acme.transport.customs.clearance.cycle

import com.acme.transport.kit.flags.Registry

/** One step of the clearance cycle calculation. */
interface ClearanceCycleModifier {
    fun describe(): String
    fun adjust(item: ClearanceCycle, value: Double): Double
}

data class PercentClearanceCycleAdjuster(var percent: Double) : ClearanceCycleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ClearanceCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedClearanceCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ClearanceCycleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ClearanceCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ClearanceCycleSequenceOffsetAdjuster(var weight: Double) : ClearanceCycleModifier {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: ClearanceCycle, value: Double): Double = value + item.sequence.toDouble() * weight
}

data class TieredClearanceCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ClearanceCycleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ClearanceCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapClearanceCycleAdjuster(var cap: Double) : ClearanceCycleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ClearanceCycle, value: Double): Double = minOf(value, cap)
}

/** Runs the clearance cycle steps in order and keeps a trace of each. */
data class ClearanceCycleEngine(
    var steps: MutableList<ClearanceCycleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ClearanceCycle): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ClearanceCycleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ClearanceCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultClearanceCycleEngine(flags: Registry): ClearanceCycleEngine {
    return ClearanceCycleEngine(
        steps = mutableListOf<ClearanceCycleModifier>(
            PercentClearanceCycleAdjuster(6.9),
            FlaggedClearanceCycleAdjuster(flags, "clearance-cycle-primary", 5.9),
            ClearanceCycleSequenceOffsetAdjuster(1.64),
            TieredClearanceCycleAdjuster(
                mutableListOf<Double>(4092.0, 6316.0),
                mutableListOf<Double>(0.97, 0.83),
            ),
            CapClearanceCycleAdjuster(35682.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 62252.7,
    )
}
