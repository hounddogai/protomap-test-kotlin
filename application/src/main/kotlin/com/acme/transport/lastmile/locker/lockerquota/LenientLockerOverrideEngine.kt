package com.acme.transport.lastmile.locker.lockerquota

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lenient locker override calculation. */
interface LenientLockerOverrideFactor {
    fun describe(): String
    fun adjust(item: LenientLockerOverride, value: Double): Double
}

data class LenientLockerOverrideStartsAtOffsetAdjuster(var weight: Double) : LenientLockerOverrideFactor {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: LenientLockerOverride, value: Double): Double {
        return value + item.startsAt.toDouble() * weight
    }
}

data class RoundingLenientLockerOverrideAdjuster(var step: Double) : LenientLockerOverrideFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientLockerOverride, value: Double): Double = roundTo(value, step)
}

data class CapLenientLockerOverrideAdjuster(var cap: Double) : LenientLockerOverrideFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientLockerOverride, value: Double): Double = minOf(value, cap)
}

data class MinimumLenientLockerOverrideAdjuster(var minimum: Double) : LenientLockerOverrideFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientLockerOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the lenient locker override steps in order and keeps a trace of each. */
data class LenientLockerOverrideEngine(
    var steps: MutableList<LenientLockerOverrideFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LenientLockerOverride): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientLockerOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLenientLockerOverrideEngine(): LenientLockerOverrideEngine {
    return LenientLockerOverrideEngine(
        steps = mutableListOf<LenientLockerOverrideFactor>(
            LenientLockerOverrideStartsAtOffsetAdjuster(1.45),
            RoundingLenientLockerOverrideAdjuster(0.5),
            CapLenientLockerOverrideAdjuster(42795.0),
            MinimumLenientLockerOverrideAdjuster(6.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 28531.2,
    )
}
