package com.acme.transport.linehaul.capacity.cycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the capacity reservation calculation. */
interface CapacityReservationAdjuster {
    fun describe(): String
    fun adjust(item: CapacityReservation, value: Double): Double
}

data class MinimumCapacityReservationAdjuster(var minimum: Double) : CapacityReservationAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CapacityReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedCapacityReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CapacityReservationAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CapacityReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingCapacityReservationAdjuster(var step: Double) : CapacityReservationAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CapacityReservation, value: Double): Double = roundTo(value, step)
}

data class CapCapacityReservationAdjuster(var cap: Double) : CapacityReservationAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CapacityReservation, value: Double): Double = minOf(value, cap)
}

data class TieredCapacityReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CapacityReservationAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CapacityReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the capacity reservation steps in order and keeps a trace of each. */
data class CapacityReservationEngine(
    var steps: MutableList<CapacityReservationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CapacityReservation): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CapacityReservationAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<CapacityReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCapacityReservationEngine(flags: Registry): CapacityReservationEngine {
    return CapacityReservationEngine(
        steps = mutableListOf<CapacityReservationAdjuster>(
            MinimumCapacityReservationAdjuster(5.3),
            FlaggedCapacityReservationAdjuster(flags, "capacity-reservation-strict", 9.9),
            RoundingCapacityReservationAdjuster(1.0),
            CapCapacityReservationAdjuster(20593.0),
            TieredCapacityReservationAdjuster(
                mutableListOf<Double>(1743.0, 6810.0, 8271.0),
                mutableListOf<Double>(0.96, 0.91, 1.13),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 92580.8,
    )
}
