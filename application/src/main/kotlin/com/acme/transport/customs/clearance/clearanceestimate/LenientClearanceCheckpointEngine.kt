package com.acme.transport.customs.clearance.clearanceestimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lenient clearance checkpoint calculation. */
interface LenientClearanceCheckpointModifier {
    fun describe(): String
    fun adjust(item: LenientClearanceCheckpoint, value: Double): Double
}

data class MinimumLenientClearanceCheckpointAdjuster(var minimum: Double) : LenientClearanceCheckpointModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientClearanceCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentLenientClearanceCheckpointAdjuster(var percent: Double) : LenientClearanceCheckpointModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientClearanceCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredLenientClearanceCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientClearanceCheckpointModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientClearanceCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedLenientClearanceCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientClearanceCheckpointModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientClearanceCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingLenientClearanceCheckpointAdjuster(var step: Double) : LenientClearanceCheckpointModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientClearanceCheckpoint, value: Double): Double = roundTo(value, step)
}

data class CapLenientClearanceCheckpointAdjuster(var cap: Double) : LenientClearanceCheckpointModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientClearanceCheckpoint, value: Double): Double = minOf(value, cap)
}

/** Runs the lenient clearance checkpoint steps in order and keeps a trace of each. */
data class LenientClearanceCheckpointEngine(
    var steps: MutableList<LenientClearanceCheckpointModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LenientClearanceCheckpoint): Double {
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

    fun evaluateAll(items: MutableList<LenientClearanceCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLenientClearanceCheckpointEngine(flags: Registry): LenientClearanceCheckpointEngine {
    return LenientClearanceCheckpointEngine(
        steps = mutableListOf<LenientClearanceCheckpointModifier>(
            MinimumLenientClearanceCheckpointAdjuster(21.8),
            PercentLenientClearanceCheckpointAdjuster(-19.9),
            TieredLenientClearanceCheckpointAdjuster(
                mutableListOf<Double>(1281.0, 8844.0),
                mutableListOf<Double>(0.86, 1.06),
            ),
            FlaggedLenientClearanceCheckpointAdjuster(flags, "lenient-clearance-checkpoint-batched", 6.5),
            RoundingLenientClearanceCheckpointAdjuster(1.0),
            CapLenientClearanceCheckpointAdjuster(44309.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 94000.2,
    )
}
