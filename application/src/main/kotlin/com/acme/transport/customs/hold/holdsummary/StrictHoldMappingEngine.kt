package com.acme.transport.customs.hold.holdsummary

import com.acme.transport.kit.flags.Registry

/** One step of the strict hold mapping calculation. */
interface StrictHoldMappingFactor {
    fun describe(): String
    fun adjust(item: StrictHoldMapping, value: Double): Double
}

data class CapStrictHoldMappingAdjuster(var cap: Double) : StrictHoldMappingFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictHoldMapping, value: Double): Double = minOf(value, cap)
}

data class FlaggedStrictHoldMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictHoldMappingFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictHoldMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class StrictHoldMappingAmountOffsetAdjuster(var weight: Double) : StrictHoldMappingFactor {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: StrictHoldMapping, value: Double): Double = value + item.amount * weight
}

/** Runs the strict hold mapping steps in order and keeps a trace of each. */
data class StrictHoldMappingEngine(
    var steps: MutableList<StrictHoldMappingFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: StrictHoldMapping): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictHoldMappingFactor = steps[index]
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

    fun evaluateAll(items: MutableList<StrictHoldMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultStrictHoldMappingEngine(flags: Registry): StrictHoldMappingEngine {
    return StrictHoldMappingEngine(
        steps = mutableListOf<StrictHoldMappingFactor>(
            CapStrictHoldMappingAdjuster(1757.0),
            FlaggedStrictHoldMappingAdjuster(flags, "strict-hold-mapping-strict", -6.1),
            StrictHoldMappingAmountOffsetAdjuster(1.7),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 3432.0,
    )
}
