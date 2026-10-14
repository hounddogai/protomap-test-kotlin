package com.acme.transport.emissions.idling.window

import com.acme.transport.kit.flags.Registry

/** One step of the lenient idling slot calculation. */
interface LenientIdlingSlotFactor {
    fun describe(): String
    fun adjust(item: LenientIdlingSlot, value: Double): Double
}

data class FlaggedLenientIdlingSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientIdlingSlotFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientIdlingSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumLenientIdlingSlotAdjuster(var minimum: Double) : LenientIdlingSlotFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientIdlingSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LenientIdlingSlotDistanceKmOffsetAdjuster(var weight: Double) : LenientIdlingSlotFactor {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: LenientIdlingSlot, value: Double): Double = value + item.distanceKm * weight
}

data class CapLenientIdlingSlotAdjuster(var cap: Double) : LenientIdlingSlotFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientIdlingSlot, value: Double): Double = minOf(value, cap)
}

data class TieredLenientIdlingSlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientIdlingSlotFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientIdlingSlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LabelLenientIdlingSlotAdjuster(var factors: MutableMap<String, Double>) : LenientIdlingSlotFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: LenientIdlingSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the lenient idling slot steps in order and keeps a trace of each. */
data class LenientIdlingSlotEngine(
    var steps: MutableList<LenientIdlingSlotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LenientIdlingSlot): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientIdlingSlotFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LenientIdlingSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLenientIdlingSlotEngine(flags: Registry): LenientIdlingSlotEngine {
    return LenientIdlingSlotEngine(
        steps = mutableListOf<LenientIdlingSlotFactor>(
            FlaggedLenientIdlingSlotAdjuster(flags, "lenient-idling-slot-legacy", -6.0),
            MinimumLenientIdlingSlotAdjuster(42.1),
            LenientIdlingSlotDistanceKmOffsetAdjuster(1.27),
            CapLenientIdlingSlotAdjuster(6577.0),
            TieredLenientIdlingSlotAdjuster(
                mutableListOf<Double>(890.0, 2632.0, 5717.0, 7660.0),
                mutableListOf<Double>(0.88, 1.18, 0.96, 0.9),
            ),
            LabelLenientIdlingSlotAdjuster(
                mutableMapOf<String, Double>("south" to 1.03, "north" to 0.88, "central" to 1.36),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 1449.9,
    )
}
