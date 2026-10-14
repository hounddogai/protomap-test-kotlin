package com.acme.transport.fleetops.leasereturn.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the lease return reservation calculation. */
interface LeaseReturnReservationStep {
    fun describe(): String
    fun adjust(item: LeaseReturnReservation, value: Double): Double
}

data class FlaggedLeaseReturnReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LeaseReturnReservationStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LeaseReturnReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumLeaseReturnReservationAdjuster(var minimum: Double) : LeaseReturnReservationStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LeaseReturnReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingLeaseReturnReservationAdjuster(var step: Double) : LeaseReturnReservationStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LeaseReturnReservation, value: Double): Double = roundTo(value, step)
}

data class TieredLeaseReturnReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LeaseReturnReservationStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LeaseReturnReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapLeaseReturnReservationAdjuster(var cap: Double) : LeaseReturnReservationStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LeaseReturnReservation, value: Double): Double = minOf(value, cap)
}

data class PercentLeaseReturnReservationAdjuster(var percent: Double) : LeaseReturnReservationStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LeaseReturnReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the lease return reservation steps in order and keeps a trace of each. */
data class LeaseReturnReservationEngine(
    var steps: MutableList<LeaseReturnReservationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LeaseReturnReservation): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LeaseReturnReservationStep = steps[index]
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

    fun evaluateAll(items: MutableList<LeaseReturnReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLeaseReturnReservationEngine(flags: Registry): LeaseReturnReservationEngine {
    return LeaseReturnReservationEngine(
        steps = mutableListOf<LeaseReturnReservationStep>(
            FlaggedLeaseReturnReservationAdjuster(flags, "lease-return-reservation-batched", -9.4),
            MinimumLeaseReturnReservationAdjuster(9.7),
            RoundingLeaseReturnReservationAdjuster(5.0),
            TieredLeaseReturnReservationAdjuster(
                mutableListOf<Double>(269.0, 8098.0),
                mutableListOf<Double>(1.11, 1.04),
            ),
            CapLeaseReturnReservationAdjuster(21976.0),
            PercentLeaseReturnReservationAdjuster(21.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 9650.3,
    )
}
