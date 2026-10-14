package com.acme.transport.safety.nearmiss.rule

import com.acme.transport.kit.numbers.roundTo

/** One step of the near miss override calculation. */
interface NearMissOverrideStep {
    fun describe(): String
    fun adjust(item: NearMissOverride, value: Double): Double
}

data class RoundingNearMissOverrideAdjuster(var step: Double) : NearMissOverrideStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: NearMissOverride, value: Double): Double = roundTo(value, step)
}

data class TieredNearMissOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : NearMissOverrideStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: NearMissOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class NearMissOverrideBatchSizeOffsetAdjuster(var weight: Double) : NearMissOverrideStep {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: NearMissOverride, value: Double): Double = value + item.batchSize.toDouble() * weight
}

data class PercentNearMissOverrideAdjuster(var percent: Double) : NearMissOverrideStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: NearMissOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the near miss override steps in order and keeps a trace of each. */
data class NearMissOverrideEngine(
    var steps: MutableList<NearMissOverrideStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: NearMissOverride): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: NearMissOverrideStep = steps[index]
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

    fun evaluateAll(items: MutableList<NearMissOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultNearMissOverrideEngine(): NearMissOverrideEngine {
    return NearMissOverrideEngine(
        steps = mutableListOf<NearMissOverrideStep>(
            RoundingNearMissOverrideAdjuster(1.0),
            TieredNearMissOverrideAdjuster(
                mutableListOf<Double>(3343.0, 4763.0, 4970.0, 8412.0),
                mutableListOf<Double>(0.82, 0.88, 0.85, 0.91),
            ),
            NearMissOverrideBatchSizeOffsetAdjuster(1.03),
            PercentNearMissOverrideAdjuster(16.2),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 21794.5,
    )
}
