package com.acme.transport.crewing.restbreak.rollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rest break rollup calculation. */
interface RestBreakRollupAdjuster {
    fun describe(): String
    fun adjust(item: RestBreakRollup, value: Double): Double
}

data class PercentRestBreakRollupAdjuster(var percent: Double) : RestBreakRollupAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RestBreakRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedRestBreakRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RestBreakRollupAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RestBreakRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRestBreakRollupAdjuster(var cap: Double) : RestBreakRollupAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RestBreakRollup, value: Double): Double = minOf(value, cap)
}

data class RestBreakRollupAmountOffsetAdjuster(var weight: Double) : RestBreakRollupAdjuster {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: RestBreakRollup, value: Double): Double = value + item.amount * weight
}

/** Runs the rest break rollup steps in order and keeps a trace of each. */
data class RestBreakRollupEngine(
    var steps: MutableList<RestBreakRollupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RestBreakRollup): Double {
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

    fun evaluateAll(items: MutableList<RestBreakRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRestBreakRollupEngine(flags: Registry): RestBreakRollupEngine {
    return RestBreakRollupEngine(
        steps = mutableListOf<RestBreakRollupAdjuster>(
            PercentRestBreakRollupAdjuster(-10.7),
            FlaggedRestBreakRollupAdjuster(flags, "rest-break-rollup-batched", -6.5),
            CapRestBreakRollupAdjuster(7657.0),
            RestBreakRollupAmountOffsetAdjuster(1.58),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 43253.6,
    )
}
