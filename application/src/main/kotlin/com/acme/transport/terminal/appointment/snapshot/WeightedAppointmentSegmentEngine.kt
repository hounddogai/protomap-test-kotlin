package com.acme.transport.terminal.appointment.snapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the weighted appointment segment calculation. */
interface WeightedAppointmentSegmentModifier {
    fun describe(): String
    fun adjust(item: WeightedAppointmentSegment, value: Double): Double
}

data class WeightedAppointmentSegmentPriorityOffsetAdjuster(
    var weight: Double,
) : WeightedAppointmentSegmentModifier {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: WeightedAppointmentSegment, value: Double): Double {
        return value + item.priority.toDouble() * weight
    }
}

data class RoundingWeightedAppointmentSegmentAdjuster(var step: Double) : WeightedAppointmentSegmentModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedAppointmentSegment, value: Double): Double = roundTo(value, step)
}

data class PercentWeightedAppointmentSegmentAdjuster(var percent: Double) : WeightedAppointmentSegmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedAppointmentSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedWeightedAppointmentSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedAppointmentSegmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedAppointmentSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredWeightedAppointmentSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedAppointmentSegmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedAppointmentSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the weighted appointment segment steps in order and keeps a trace of each. */
data class WeightedAppointmentSegmentEngine(
    var steps: MutableList<WeightedAppointmentSegmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: WeightedAppointmentSegment): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedAppointmentSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultWeightedAppointmentSegmentEngine(flags: Registry): WeightedAppointmentSegmentEngine {
    return WeightedAppointmentSegmentEngine(
        steps = mutableListOf<WeightedAppointmentSegmentModifier>(
            WeightedAppointmentSegmentPriorityOffsetAdjuster(1.38),
            RoundingWeightedAppointmentSegmentAdjuster(0.25),
            PercentWeightedAppointmentSegmentAdjuster(19.8),
            FlaggedWeightedAppointmentSegmentAdjuster(flags, "weighted-appointment-segment-regional", -9.9),
            TieredWeightedAppointmentSegmentAdjuster(
                mutableListOf<Double>(214.0, 798.0, 912.0, 4148.0),
                mutableListOf<Double>(1.06, 0.99, 1.06, 0.87),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 8851.2,
    )
}
