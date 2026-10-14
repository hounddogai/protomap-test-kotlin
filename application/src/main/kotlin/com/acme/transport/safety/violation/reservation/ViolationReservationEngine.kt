package com.acme.transport.safety.violation.reservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the violation reservation calculation. */
interface ViolationReservationStep {
    fun describe(): String
    fun adjust(item: ViolationReservation, value: Double): Double
}

data class RoundingViolationReservationAdjuster(var step: Double) : ViolationReservationStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ViolationReservation, value: Double): Double = roundTo(value, step)
}

data class CapViolationReservationAdjuster(var cap: Double) : ViolationReservationStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ViolationReservation, value: Double): Double = minOf(value, cap)
}

data class SegmentViolationReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : ViolationReservationStep {
    override fun describe(): String = "by segment"

    override fun adjust(item: ViolationReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredViolationReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ViolationReservationStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ViolationReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedViolationReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ViolationReservationStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ViolationReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the violation reservation steps in order and keeps a trace of each. */
data class ViolationReservationEngine(
    var steps: MutableList<ViolationReservationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ViolationReservation): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ViolationReservationStep = steps[index]
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

    fun evaluateAll(items: MutableList<ViolationReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultViolationReservationEngine(flags: Registry): ViolationReservationEngine {
    return ViolationReservationEngine(
        steps = mutableListOf<ViolationReservationStep>(
            RoundingViolationReservationAdjuster(0.05),
            CapViolationReservationAdjuster(43738.0),
            SegmentViolationReservationAdjuster(mutableMapOf<String, Double>("east" to 1.39, "north" to 1.2)),
            TieredViolationReservationAdjuster(
                mutableListOf<Double>(5259.0, 6690.0, 8291.0),
                mutableListOf<Double>(1.17, 0.8, 0.92),
            ),
            FlaggedViolationReservationAdjuster(flags, "violation-reservation-legacy", -6.2),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 96686.2,
    )
}
