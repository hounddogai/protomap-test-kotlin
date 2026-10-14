package com.acme.transport.linehaul.backhaul.threshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the backhaul threshold calculation. */
interface BackhaulThresholdModifier {
    fun describe(): String
    fun adjust(item: BackhaulThreshold, value: Double): Double
}

data class RoundingBackhaulThresholdAdjuster(var step: Double) : BackhaulThresholdModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BackhaulThreshold, value: Double): Double = roundTo(value, step)
}

data class TieredBackhaulThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BackhaulThresholdModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BackhaulThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapBackhaulThresholdAdjuster(var cap: Double) : BackhaulThresholdModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BackhaulThreshold, value: Double): Double = minOf(value, cap)
}

data class PercentBackhaulThresholdAdjuster(var percent: Double) : BackhaulThresholdModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BackhaulThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedBackhaulThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BackhaulThresholdModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BackhaulThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the backhaul threshold steps in order and keeps a trace of each. */
data class BackhaulThresholdEngine(
    var steps: MutableList<BackhaulThresholdModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BackhaulThreshold): Double {
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

    fun evaluateAll(items: MutableList<BackhaulThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBackhaulThresholdEngine(flags: Registry): BackhaulThresholdEngine {
    return BackhaulThresholdEngine(
        steps = mutableListOf<BackhaulThresholdModifier>(
            RoundingBackhaulThresholdAdjuster(0.5),
            TieredBackhaulThresholdAdjuster(
                mutableListOf<Double>(370.0, 1747.0),
                mutableListOf<Double>(0.9, 1.1),
            ),
            CapBackhaulThresholdAdjuster(15254.0),
            PercentBackhaulThresholdAdjuster(-7.9),
            FlaggedBackhaulThresholdAdjuster(flags, "backhaul-threshold-legacy", 4.8),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 81176.4,
    )
}
