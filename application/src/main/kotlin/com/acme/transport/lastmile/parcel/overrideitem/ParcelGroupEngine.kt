package com.acme.transport.lastmile.parcel.overrideitem

import com.acme.transport.kit.numbers.roundTo

/** One step of the parcel group calculation. */
interface ParcelGroupAdjuster {
    fun describe(): String
    fun adjust(item: ParcelGroup, value: Double): Double
}

data class NameParcelGroupAdjuster(var factors: MutableMap<String, Double>) : ParcelGroupAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: ParcelGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapParcelGroupAdjuster(var cap: Double) : ParcelGroupAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ParcelGroup, value: Double): Double = minOf(value, cap)
}

data class RoundingParcelGroupAdjuster(var step: Double) : ParcelGroupAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ParcelGroup, value: Double): Double = roundTo(value, step)
}

data class MinimumParcelGroupAdjuster(var minimum: Double) : ParcelGroupAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ParcelGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ParcelGroupCapacityOffsetAdjuster(var weight: Double) : ParcelGroupAdjuster {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: ParcelGroup, value: Double): Double = value + item.capacity.toDouble() * weight
}

/** Runs the parcel group steps in order and keeps a trace of each. */
data class ParcelGroupEngine(
    var steps: MutableList<ParcelGroupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ParcelGroup): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ParcelGroupAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ParcelGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultParcelGroupEngine(): ParcelGroupEngine {
    return ParcelGroupEngine(
        steps = mutableListOf<ParcelGroupAdjuster>(
            NameParcelGroupAdjuster(mutableMapOf<String, Double>("central" to 1.32, "online" to 0.92)),
            CapParcelGroupAdjuster(13583.0),
            RoundingParcelGroupAdjuster(0.1),
            MinimumParcelGroupAdjuster(5.0),
            ParcelGroupCapacityOffsetAdjuster(1.23),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 88810.3,
    )
}
