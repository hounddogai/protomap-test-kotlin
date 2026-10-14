package com.acme.transport.safety.incident.incidentsegment

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the primary incident segment calculation. */
interface PrimaryIncidentSegmentModifier {
    fun describe(): String
    fun adjust(item: PrimaryIncidentSegment, value: Double): Double
}

data class PrimaryIncidentSegmentAmountOffsetAdjuster(var weight: Double) : PrimaryIncidentSegmentModifier {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: PrimaryIncidentSegment, value: Double): Double = value + item.amount * weight
}

data class PercentPrimaryIncidentSegmentAdjuster(var percent: Double) : PrimaryIncidentSegmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryIncidentSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingPrimaryIncidentSegmentAdjuster(var step: Double) : PrimaryIncidentSegmentModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryIncidentSegment, value: Double): Double = roundTo(value, step)
}

data class CapPrimaryIncidentSegmentAdjuster(var cap: Double) : PrimaryIncidentSegmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryIncidentSegment, value: Double): Double = minOf(value, cap)
}

data class TieredPrimaryIncidentSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryIncidentSegmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryIncidentSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumPrimaryIncidentSegmentAdjuster(var minimum: Double) : PrimaryIncidentSegmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryIncidentSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the primary incident segment steps in order and keeps a trace of each. */
data class PrimaryIncidentSegmentEngine(
    var steps: MutableList<PrimaryIncidentSegmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PrimaryIncidentSegment): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryIncidentSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPrimaryIncidentSegmentEngine(): PrimaryIncidentSegmentEngine {
    return PrimaryIncidentSegmentEngine(
        steps = mutableListOf<PrimaryIncidentSegmentModifier>(
            PrimaryIncidentSegmentAmountOffsetAdjuster(0.48),
            PercentPrimaryIncidentSegmentAdjuster(22.9),
            RoundingPrimaryIncidentSegmentAdjuster(0.1),
            CapPrimaryIncidentSegmentAdjuster(47562.0),
            TieredPrimaryIncidentSegmentAdjuster(
                mutableListOf<Double>(6056.0, 7470.0, 8012.0, 8431.0),
                mutableListOf<Double>(0.85, 1.02, 1.06, 1.0),
            ),
            MinimumPrimaryIncidentSegmentAdjuster(48.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 41043.9,
    )
}
