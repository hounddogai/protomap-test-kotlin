package com.acme.transport.lastmile.locker.schedule

/** One step of the locker override calculation. */
interface LockerOverrideModifier {
    fun describe(): String
    fun adjust(item: LockerOverride, value: Double): Double
}

data class CapLockerOverrideAdjuster(var cap: Double) : LockerOverrideModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LockerOverride, value: Double): Double = minOf(value, cap)
}

data class MinimumLockerOverrideAdjuster(var minimum: Double) : LockerOverrideModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LockerOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentLockerOverrideAdjuster(var percent: Double) : LockerOverrideModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LockerOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LockerOverrideDistanceKmOffsetAdjuster(var weight: Double) : LockerOverrideModifier {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: LockerOverride, value: Double): Double = value + item.distanceKm * weight
}

/** Runs the locker override steps in order and keeps a trace of each. */
data class LockerOverrideEngine(
    var steps: MutableList<LockerOverrideModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LockerOverride): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LockerOverrideModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LockerOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLockerOverrideEngine(): LockerOverrideEngine {
    return LockerOverrideEngine(
        steps = mutableListOf<LockerOverrideModifier>(
            CapLockerOverrideAdjuster(23120.0),
            MinimumLockerOverrideAdjuster(13.2),
            PercentLockerOverrideAdjuster(-22.7),
            LockerOverrideDistanceKmOffsetAdjuster(0.37),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 25258.6,
    )
}
