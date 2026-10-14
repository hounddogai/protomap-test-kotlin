package com.acme.transport.fleetops.tire.plan

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tire exception calculation. */
interface TireExceptionFactor {
    fun describe(): String
    fun adjust(item: TireException, value: Double): Double
}

data class TieredTireExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TireExceptionFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TireException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapTireExceptionAdjuster(var cap: Double) : TireExceptionFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TireException, value: Double): Double = minOf(value, cap)
}

data class FlaggedTireExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TireExceptionFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TireException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the tire exception steps in order and keeps a trace of each. */
data class TireExceptionEngine(
    var steps: MutableList<TireExceptionFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TireException): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TireException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTireExceptionEngine(flags: Registry): TireExceptionEngine {
    return TireExceptionEngine(
        steps = mutableListOf<TireExceptionFactor>(
            TieredTireExceptionAdjuster(
                mutableListOf<Double>(5494.0, 9334.0),
                mutableListOf<Double>(1.12, 1.16),
            ),
            CapTireExceptionAdjuster(47799.0),
            FlaggedTireExceptionAdjuster(flags, "tire-exception-capped", 5.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 74524.5,
    )
}
