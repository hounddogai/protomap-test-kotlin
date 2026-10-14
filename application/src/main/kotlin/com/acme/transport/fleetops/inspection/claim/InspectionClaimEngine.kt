package com.acme.transport.fleetops.inspection.claim

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the inspection claim calculation. */
interface InspectionClaimStep {
    fun describe(): String
    fun adjust(item: InspectionClaim, value: Double): Double
}

data class PercentInspectionClaimAdjuster(var percent: Double) : InspectionClaimStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: InspectionClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingInspectionClaimAdjuster(var step: Double) : InspectionClaimStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: InspectionClaim, value: Double): Double = roundTo(value, step)
}

data class InspectionClaimDurationMinutesOffsetAdjuster(var weight: Double) : InspectionClaimStep {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: InspectionClaim, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

/** Runs the inspection claim steps in order and keeps a trace of each. */
data class InspectionClaimEngine(
    var steps: MutableList<InspectionClaimStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: InspectionClaim): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<InspectionClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultInspectionClaimEngine(): InspectionClaimEngine {
    return InspectionClaimEngine(
        steps = mutableListOf<InspectionClaimStep>(
            PercentInspectionClaimAdjuster(-15.0),
            RoundingInspectionClaimAdjuster(0.25),
            InspectionClaimDurationMinutesOffsetAdjuster(1.81),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 75020.5,
    )
}
