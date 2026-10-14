package com.acme.transport.lastmile.parcel.baseline

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lenient parcel slot calculation. */
interface LenientParcelSlotFactor {
    fun describe(): String
    fun adjust(item: LenientParcelSlot, value: Double): Double
}

data class PercentLenientParcelSlotAdjuster(var percent: Double) : LenientParcelSlotFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientParcelSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumLenientParcelSlotAdjuster(var minimum: Double) : LenientParcelSlotFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientParcelSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class SourceLenientParcelSlotAdjuster(var factors: MutableMap<String, Double>) : LenientParcelSlotFactor {
    override fun describe(): String = "by source"

    override fun adjust(item: LenientParcelSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedLenientParcelSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientParcelSlotFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientParcelSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the lenient parcel slot steps in order and keeps a trace of each. */
data class LenientParcelSlotEngine(
    var steps: MutableList<LenientParcelSlotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LenientParcelSlot): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientParcelSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLenientParcelSlotEngine(flags: Registry): LenientParcelSlotEngine {
    return LenientParcelSlotEngine(
        steps = mutableListOf<LenientParcelSlotFactor>(
            PercentLenientParcelSlotAdjuster(14.5),
            MinimumLenientParcelSlotAdjuster(6.3),
            SourceLenientParcelSlotAdjuster(mutableMapOf<String, Double>("north" to 0.94)),
            FlaggedLenientParcelSlotAdjuster(flags, "lenient-parcel-slot-default", -7.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 58752.1,
    )
}
