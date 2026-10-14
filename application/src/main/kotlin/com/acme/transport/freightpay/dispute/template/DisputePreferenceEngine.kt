package com.acme.transport.freightpay.dispute.template

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the dispute preference calculation. */
interface DisputePreferenceModifier {
    fun describe(): String
    fun adjust(item: DisputePreference, value: Double): Double
}

data class CapDisputePreferenceAdjuster(var cap: Double) : DisputePreferenceModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DisputePreference, value: Double): Double = minOf(value, cap)
}

data class LabelDisputePreferenceAdjuster(var factors: MutableMap<String, Double>) : DisputePreferenceModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: DisputePreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class DisputePreferenceDistanceKmOffsetAdjuster(var weight: Double) : DisputePreferenceModifier {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: DisputePreference, value: Double): Double = value + item.distanceKm * weight
}

data class RoundingDisputePreferenceAdjuster(var step: Double) : DisputePreferenceModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DisputePreference, value: Double): Double = roundTo(value, step)
}

/** Runs the dispute preference steps in order and keeps a trace of each. */
data class DisputePreferenceEngine(
    var steps: MutableList<DisputePreferenceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DisputePreference): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DisputePreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDisputePreferenceEngine(): DisputePreferenceEngine {
    return DisputePreferenceEngine(
        steps = mutableListOf<DisputePreferenceModifier>(
            CapDisputePreferenceAdjuster(41387.0),
            LabelDisputePreferenceAdjuster(mutableMapOf<String, Double>("retail" to 1.29)),
            DisputePreferenceDistanceKmOffsetAdjuster(1.32),
            RoundingDisputePreferenceAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 49070.3,
    )
}
