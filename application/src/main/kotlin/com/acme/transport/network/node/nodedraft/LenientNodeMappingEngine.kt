package com.acme.transport.network.node.nodedraft

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lenient node mapping calculation. */
interface LenientNodeMappingAdjuster {
    fun describe(): String
    fun adjust(item: LenientNodeMapping, value: Double): Double
}

data class FlaggedLenientNodeMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientNodeMappingAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientNodeMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingLenientNodeMappingAdjuster(var step: Double) : LenientNodeMappingAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientNodeMapping, value: Double): Double = roundTo(value, step)
}

data class TieredLenientNodeMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientNodeMappingAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientNodeMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LenientNodeMappingOdometerOffsetAdjuster(var weight: Double) : LenientNodeMappingAdjuster {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: LenientNodeMapping, value: Double): Double = value + item.odometer.toDouble() * weight
}

/** Runs the lenient node mapping steps in order and keeps a trace of each. */
data class LenientNodeMappingEngine(
    var steps: MutableList<LenientNodeMappingAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LenientNodeMapping): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientNodeMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLenientNodeMappingEngine(flags: Registry): LenientNodeMappingEngine {
    return LenientNodeMappingEngine(
        steps = mutableListOf<LenientNodeMappingAdjuster>(
            FlaggedLenientNodeMappingAdjuster(flags, "lenient-node-mapping-regional", -3.5),
            RoundingLenientNodeMappingAdjuster(5.0),
            TieredLenientNodeMappingAdjuster(
                mutableListOf<Double>(1539.0, 3011.0, 4170.0, 7670.0),
                mutableListOf<Double>(0.97, 1.12, 1.14, 1.17),
            ),
            LenientNodeMappingOdometerOffsetAdjuster(1.04),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 80507.5,
    )
}
