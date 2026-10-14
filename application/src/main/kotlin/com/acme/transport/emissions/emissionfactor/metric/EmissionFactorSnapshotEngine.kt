package com.acme.transport.emissions.emissionfactor.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the emission factor snapshot calculation. */
interface EmissionFactorSnapshotStep {
    fun describe(): String
    fun adjust(item: EmissionFactorSnapshot, value: Double): Double
}

data class EmissionFactorSnapshotCostOffsetAdjuster(var weight: Double) : EmissionFactorSnapshotStep {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: EmissionFactorSnapshot, value: Double): Double = value + item.cost * weight
}

data class FlaggedEmissionFactorSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : EmissionFactorSnapshotStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: EmissionFactorSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingEmissionFactorSnapshotAdjuster(var step: Double) : EmissionFactorSnapshotStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: EmissionFactorSnapshot, value: Double): Double = roundTo(value, step)
}

data class MinimumEmissionFactorSnapshotAdjuster(var minimum: Double) : EmissionFactorSnapshotStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EmissionFactorSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentEmissionFactorSnapshotAdjuster(var percent: Double) : EmissionFactorSnapshotStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: EmissionFactorSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ChannelEmissionFactorSnapshotAdjuster(
    var factors: MutableMap<String, Double>,
) : EmissionFactorSnapshotStep {
    override fun describe(): String = "by channel"

    override fun adjust(item: EmissionFactorSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the emission factor snapshot steps in order and keeps a trace of each. */
data class EmissionFactorSnapshotEngine(
    var steps: MutableList<EmissionFactorSnapshotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: EmissionFactorSnapshot): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: EmissionFactorSnapshotStep = steps[index]
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

    fun evaluateAll(items: MutableList<EmissionFactorSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultEmissionFactorSnapshotEngine(flags: Registry): EmissionFactorSnapshotEngine {
    return EmissionFactorSnapshotEngine(
        steps = mutableListOf<EmissionFactorSnapshotStep>(
            EmissionFactorSnapshotCostOffsetAdjuster(0.62),
            FlaggedEmissionFactorSnapshotAdjuster(flags, "emission-factor-snapshot-seasonal", -3.7),
            RoundingEmissionFactorSnapshotAdjuster(0.05),
            MinimumEmissionFactorSnapshotAdjuster(31.1),
            PercentEmissionFactorSnapshotAdjuster(3.9),
            ChannelEmissionFactorSnapshotAdjuster(
                mutableMapOf<String, Double>("retail" to 1.01, "central" to 0.77),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 37840.8,
    )
}
