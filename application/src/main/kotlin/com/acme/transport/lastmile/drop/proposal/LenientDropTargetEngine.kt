package com.acme.transport.lastmile.drop.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lenient drop target calculation. */
interface LenientDropTargetFactor {
    fun describe(): String
    fun adjust(item: LenientDropTarget, value: Double): Double
}

data class FlaggedLenientDropTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientDropTargetFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientDropTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredLenientDropTargetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientDropTargetFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientDropTarget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumLenientDropTargetAdjuster(var minimum: Double) : LenientDropTargetFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientDropTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LenientDropTargetCapacityOffsetAdjuster(var weight: Double) : LenientDropTargetFactor {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: LenientDropTarget, value: Double): Double = value + item.capacity.toDouble() * weight
}

/** Runs the lenient drop target steps in order and keeps a trace of each. */
data class LenientDropTargetEngine(
    var steps: MutableList<LenientDropTargetFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LenientDropTarget): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientDropTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLenientDropTargetEngine(flags: Registry): LenientDropTargetEngine {
    return LenientDropTargetEngine(
        steps = mutableListOf<LenientDropTargetFactor>(
            FlaggedLenientDropTargetAdjuster(flags, "lenient-drop-target-scheduled", -8.5),
            TieredLenientDropTargetAdjuster(
                mutableListOf<Double>(482.0, 4175.0, 4789.0, 9377.0),
                mutableListOf<Double>(0.99, 0.82, 0.82, 0.97),
            ),
            MinimumLenientDropTargetAdjuster(41.5),
            LenientDropTargetCapacityOffsetAdjuster(1.73),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 14971.5,
    )
}
