package com.acme.transport.freightpay.accessorial.reservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lenient accessorial allocation calculation. */
interface LenientAccessorialAllocationAdjuster {
    fun describe(): String
    fun adjust(item: LenientAccessorialAllocation, value: Double): Double
}

data class RoundingLenientAccessorialAllocationAdjuster(
    var step: Double,
) : LenientAccessorialAllocationAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientAccessorialAllocation, value: Double): Double = roundTo(value, step)
}

data class FlaggedLenientAccessorialAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientAccessorialAllocationAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientAccessorialAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LenientAccessorialAllocationQuantityOffsetAdjuster(
    var weight: Double,
) : LenientAccessorialAllocationAdjuster {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: LenientAccessorialAllocation, value: Double): Double {
        return value + item.quantity.toDouble() * weight
    }
}

data class SourceLenientAccessorialAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientAccessorialAllocationAdjuster {
    override fun describe(): String = "by source"

    override fun adjust(item: LenientAccessorialAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredLenientAccessorialAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientAccessorialAllocationAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientAccessorialAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the lenient accessorial allocation steps in order and keeps a trace of each. */
data class LenientAccessorialAllocationEngine(
    var steps: MutableList<LenientAccessorialAllocationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LenientAccessorialAllocation): Double {
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

    fun evaluateAll(items: MutableList<LenientAccessorialAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLenientAccessorialAllocationEngine(flags: Registry): LenientAccessorialAllocationEngine {
    return LenientAccessorialAllocationEngine(
        steps = mutableListOf<LenientAccessorialAllocationAdjuster>(
            RoundingLenientAccessorialAllocationAdjuster(0.25),
            FlaggedLenientAccessorialAllocationAdjuster(flags, "lenient-accessorial-allocation-lenient", 0.6),
            LenientAccessorialAllocationQuantityOffsetAdjuster(1.26),
            SourceLenientAccessorialAllocationAdjuster(
                mutableMapOf<String, Double>("online" to 1.38, "east" to 0.74),
            ),
            TieredLenientAccessorialAllocationAdjuster(
                mutableListOf<Double>(7844.0, 9096.0),
                mutableListOf<Double>(0.96, 1.17),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 54771.0,
    )
}
