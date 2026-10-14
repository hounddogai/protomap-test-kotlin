package com.acme.transport.customs.inspection.metric

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the inspection profile calculation. */
interface InspectionProfileStep {
    fun describe(): String
    fun adjust(item: InspectionProfile, value: Double): Double
}

data class CapInspectionProfileAdjuster(var cap: Double) : InspectionProfileStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: InspectionProfile, value: Double): Double = minOf(value, cap)
}

data class TieredInspectionProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : InspectionProfileStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: InspectionProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class InspectionProfileExpiresAtOffsetAdjuster(var weight: Double) : InspectionProfileStep {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: InspectionProfile, value: Double): Double = value + item.expiresAt.toDouble() * weight
}

data class PercentInspectionProfileAdjuster(var percent: Double) : InspectionProfileStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: InspectionProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the inspection profile steps in order and keeps a trace of each. */
data class InspectionProfileEngine(
    var steps: MutableList<InspectionProfileStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: InspectionProfile): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<InspectionProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultInspectionProfileEngine(): InspectionProfileEngine {
    return InspectionProfileEngine(
        steps = mutableListOf<InspectionProfileStep>(
            CapInspectionProfileAdjuster(44256.0),
            TieredInspectionProfileAdjuster(
                mutableListOf<Double>(2279.0, 6974.0, 8607.0),
                mutableListOf<Double>(1.2, 1.14, 0.85),
            ),
            InspectionProfileExpiresAtOffsetAdjuster(1.36),
            PercentInspectionProfileAdjuster(-2.7),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 11702.9,
    )
}
