package com.acme.transport.crewing.qualification.target

import com.acme.transport.kit.flags.Registry

/** One step of the qualification reservation calculation. */
interface QualificationReservationModifier {
    fun describe(): String
    fun adjust(item: QualificationReservation, value: Double): Double
}

data class TieredQualificationReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : QualificationReservationModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: QualificationReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedQualificationReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : QualificationReservationModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: QualificationReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumQualificationReservationAdjuster(var minimum: Double) : QualificationReservationModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: QualificationReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the qualification reservation steps in order and keeps a trace of each. */
data class QualificationReservationEngine(
    var steps: MutableList<QualificationReservationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: QualificationReservation): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: QualificationReservationModifier = steps[index]
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

    fun evaluateAll(items: MutableList<QualificationReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultQualificationReservationEngine(flags: Registry): QualificationReservationEngine {
    return QualificationReservationEngine(
        steps = mutableListOf<QualificationReservationModifier>(
            TieredQualificationReservationAdjuster(
                mutableListOf<Double>(3687.0, 4634.0, 7149.0),
                mutableListOf<Double>(1.01, 1.02, 0.85),
            ),
            FlaggedQualificationReservationAdjuster(flags, "qualification-reservation-legacy", -6.7),
            MinimumQualificationReservationAdjuster(2.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 19548.7,
    )
}
