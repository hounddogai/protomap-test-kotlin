package com.acme.transport.emissions.emissionfactor.schedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the emission factor segment calculation. */
interface EmissionFactorSegmentStep {
    fun describe(): String
    fun adjust(item: EmissionFactorSegment, value: Double): Double
}

data class RoundingEmissionFactorSegmentAdjuster(var step: Double) : EmissionFactorSegmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: EmissionFactorSegment, value: Double): Double = roundTo(value, step)
}

data class EmissionFactorSegmentLimitOffsetAdjuster(var weight: Double) : EmissionFactorSegmentStep {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: EmissionFactorSegment, value: Double): Double = value + item.limit.toDouble() * weight
}

data class FlaggedEmissionFactorSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : EmissionFactorSegmentStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: EmissionFactorSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the emission factor segment steps in order and keeps a trace of each. */
data class EmissionFactorSegmentEngine(
    var steps: MutableList<EmissionFactorSegmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: EmissionFactorSegment): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: EmissionFactorSegmentStep = steps[index]
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

    fun evaluateAll(items: MutableList<EmissionFactorSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultEmissionFactorSegmentEngine(flags: Registry): EmissionFactorSegmentEngine {
    return EmissionFactorSegmentEngine(
        steps = mutableListOf<EmissionFactorSegmentStep>(
            RoundingEmissionFactorSegmentAdjuster(0.25),
            EmissionFactorSegmentLimitOffsetAdjuster(1.3),
            FlaggedEmissionFactorSegmentAdjuster(flags, "emission-factor-segment-tiered", 3.6),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 24368.4,
    )
}
