package com.acme.transport.linehaul.relay.schedule

import com.acme.transport.kit.numbers.roundTo

/** One step of the tiered relay override calculation. */
interface TieredRelayOverrideStep {
    fun describe(): String
    fun adjust(item: TieredRelayOverride, value: Double): Double
}

data class TieredRelayOverrideWindowDaysOffsetAdjuster(var weight: Double) : TieredRelayOverrideStep {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: TieredRelayOverride, value: Double): Double {
        return value + item.windowDays.toDouble() * weight
    }
}

data class PercentTieredRelayOverrideAdjuster(var percent: Double) : TieredRelayOverrideStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredRelayOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingTieredRelayOverrideAdjuster(var step: Double) : TieredRelayOverrideStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredRelayOverride, value: Double): Double = roundTo(value, step)
}

/** Runs the tiered relay override steps in order and keeps a trace of each. */
data class TieredRelayOverrideEngine(
    var steps: MutableList<TieredRelayOverrideStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TieredRelayOverride): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredRelayOverrideStep = steps[index]
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

    fun evaluateAll(items: MutableList<TieredRelayOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTieredRelayOverrideEngine(): TieredRelayOverrideEngine {
    return TieredRelayOverrideEngine(
        steps = mutableListOf<TieredRelayOverrideStep>(
            TieredRelayOverrideWindowDaysOffsetAdjuster(0.6),
            PercentTieredRelayOverrideAdjuster(-21.3),
            RoundingTieredRelayOverrideAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 75577.4,
    )
}
