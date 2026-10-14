package com.acme.transport.network.cutoff.template

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the default cutoff estimate calculation. */
interface DefaultCutoffEstimateStep {
    fun describe(): String
    fun adjust(item: DefaultCutoffEstimate, value: Double): Double
}

data class RoundingDefaultCutoffEstimateAdjuster(var step: Double) : DefaultCutoffEstimateStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultCutoffEstimate, value: Double): Double = roundTo(value, step)
}

data class FlaggedDefaultCutoffEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultCutoffEstimateStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultCutoffEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapDefaultCutoffEstimateAdjuster(var cap: Double) : DefaultCutoffEstimateStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultCutoffEstimate, value: Double): Double = minOf(value, cap)
}

data class DefaultCutoffEstimateFloorValueOffsetAdjuster(var weight: Double) : DefaultCutoffEstimateStep {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: DefaultCutoffEstimate, value: Double): Double = value + item.floorValue * weight
}

/** Runs the default cutoff estimate steps in order and keeps a trace of each. */
data class DefaultCutoffEstimateEngine(
    var steps: MutableList<DefaultCutoffEstimateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DefaultCutoffEstimate): Double {
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

    fun evaluateAll(items: MutableList<DefaultCutoffEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDefaultCutoffEstimateEngine(flags: Registry): DefaultCutoffEstimateEngine {
    return DefaultCutoffEstimateEngine(
        steps = mutableListOf<DefaultCutoffEstimateStep>(
            RoundingDefaultCutoffEstimateAdjuster(5.0),
            FlaggedDefaultCutoffEstimateAdjuster(flags, "default-cutoff-estimate-legacy", 3.8),
            CapDefaultCutoffEstimateAdjuster(18600.0),
            DefaultCutoffEstimateFloorValueOffsetAdjuster(0.37),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 95540.3,
    )
}
