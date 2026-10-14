package com.acme.transport.safety.violation.snapshot

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped violation threshold calculation. */
interface CappedViolationThresholdStep {
    fun describe(): String
    fun adjust(item: CappedViolationThreshold, value: Double): Double
}

data class TieredCappedViolationThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedViolationThresholdStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedViolationThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SourceCappedViolationThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedViolationThresholdStep {
    override fun describe(): String = "by source"

    override fun adjust(item: CappedViolationThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingCappedViolationThresholdAdjuster(var step: Double) : CappedViolationThresholdStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedViolationThreshold, value: Double): Double = roundTo(value, step)
}

data class MinimumCappedViolationThresholdAdjuster(var minimum: Double) : CappedViolationThresholdStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedViolationThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the capped violation threshold steps in order and keeps a trace of each. */
data class CappedViolationThresholdEngine(
    var steps: MutableList<CappedViolationThresholdStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CappedViolationThreshold): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedViolationThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCappedViolationThresholdEngine(): CappedViolationThresholdEngine {
    return CappedViolationThresholdEngine(
        steps = mutableListOf<CappedViolationThresholdStep>(
            TieredCappedViolationThresholdAdjuster(
                mutableListOf<Double>(205.0, 1057.0, 6284.0, 8966.0),
                mutableListOf<Double>(1.0, 1.07, 1.13, 0.82),
            ),
            SourceCappedViolationThresholdAdjuster(
                mutableMapOf<String, Double>("north" to 1.15, "retail" to 0.99),
            ),
            RoundingCappedViolationThresholdAdjuster(0.1),
            MinimumCappedViolationThresholdAdjuster(18.6),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 28970.9,
    )
}
