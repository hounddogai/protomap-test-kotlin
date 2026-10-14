package com.acme.transport.fleetops.leasereturn.template

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the strict lease return reservation calculation. */
interface StrictLeaseReturnReservationAdjuster {
    fun describe(): String
    fun adjust(item: StrictLeaseReturnReservation, value: Double): Double
}

data class RoundingStrictLeaseReturnReservationAdjuster(
    var step: Double,
) : StrictLeaseReturnReservationAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictLeaseReturnReservation, value: Double): Double = roundTo(value, step)
}

data class FlaggedStrictLeaseReturnReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictLeaseReturnReservationAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictLeaseReturnReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class StrictLeaseReturnReservationVersionOffsetAdjuster(
    var weight: Double,
) : StrictLeaseReturnReservationAdjuster {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: StrictLeaseReturnReservation, value: Double): Double {
        return value + item.version.toDouble() * weight
    }
}

data class CategoryStrictLeaseReturnReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictLeaseReturnReservationAdjuster {
    override fun describe(): String = "by category"

    override fun adjust(item: StrictLeaseReturnReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentStrictLeaseReturnReservationAdjuster(
    var percent: Double,
) : StrictLeaseReturnReservationAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictLeaseReturnReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the strict lease return reservation steps in order and keeps a trace of each. */
data class StrictLeaseReturnReservationEngine(
    var steps: MutableList<StrictLeaseReturnReservationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: StrictLeaseReturnReservation): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictLeaseReturnReservationAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<StrictLeaseReturnReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultStrictLeaseReturnReservationEngine(flags: Registry): StrictLeaseReturnReservationEngine {
    return StrictLeaseReturnReservationEngine(
        steps = mutableListOf<StrictLeaseReturnReservationAdjuster>(
            RoundingStrictLeaseReturnReservationAdjuster(0.1),
            FlaggedStrictLeaseReturnReservationAdjuster(flags, "strict-lease-return-reservation-rolling", 9.7),
            StrictLeaseReturnReservationVersionOffsetAdjuster(0.5),
            CategoryStrictLeaseReturnReservationAdjuster(
                mutableMapOf<String, Double>("central" to 1.36, "north" to 0.74),
            ),
            PercentStrictLeaseReturnReservationAdjuster(-15.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 6515.4,
    )
}
