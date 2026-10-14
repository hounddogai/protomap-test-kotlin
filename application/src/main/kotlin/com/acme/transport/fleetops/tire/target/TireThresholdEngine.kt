package com.acme.transport.fleetops.tire.target

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tire threshold calculation. */
interface TireThresholdModifier {
    fun describe(): String
    fun adjust(item: TireThreshold, value: Double): Double
}

data class TitleTireThresholdAdjuster(var factors: MutableMap<String, Double>) : TireThresholdModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: TireThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedTireThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TireThresholdModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TireThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentTireThresholdAdjuster(var percent: Double) : TireThresholdModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TireThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingTireThresholdAdjuster(var step: Double) : TireThresholdModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TireThreshold, value: Double): Double = roundTo(value, step)
}

/** Runs the tire threshold steps in order and keeps a trace of each. */
data class TireThresholdEngine(
    var steps: MutableList<TireThresholdModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TireThreshold): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TireThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTireThresholdEngine(flags: Registry): TireThresholdEngine {
    return TireThresholdEngine(
        steps = mutableListOf<TireThresholdModifier>(
            TitleTireThresholdAdjuster(mutableMapOf<String, Double>("retail" to 1.02, "online" to 0.7)),
            FlaggedTireThresholdAdjuster(flags, "tire-threshold-rolling", -4.6),
            PercentTireThresholdAdjuster(20.7),
            RoundingTireThresholdAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 15313.8,
    )
}
