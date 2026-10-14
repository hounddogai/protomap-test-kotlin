package com.acme.transport.linehaul.relay.rule

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the relay template calculation. */
interface RelayTemplateStep {
    fun describe(): String
    fun adjust(item: RelayTemplate, value: Double): Double
}

data class RoundingRelayTemplateAdjuster(var step: Double) : RelayTemplateStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RelayTemplate, value: Double): Double = roundTo(value, step)
}

data class RelayTemplateThresholdOffsetAdjuster(var weight: Double) : RelayTemplateStep {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: RelayTemplate, value: Double): Double = value + item.threshold * weight
}

data class TieredRelayTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RelayTemplateStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RelayTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class ChannelRelayTemplateAdjuster(var factors: MutableMap<String, Double>) : RelayTemplateStep {
    override fun describe(): String = "by channel"

    override fun adjust(item: RelayTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the relay template steps in order and keeps a trace of each. */
data class RelayTemplateEngine(
    var steps: MutableList<RelayTemplateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RelayTemplate): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RelayTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRelayTemplateEngine(): RelayTemplateEngine {
    return RelayTemplateEngine(
        steps = mutableListOf<RelayTemplateStep>(
            RoundingRelayTemplateAdjuster(0.25),
            RelayTemplateThresholdOffsetAdjuster(1.18),
            TieredRelayTemplateAdjuster(mutableListOf<Double>(598.0, 5393.0), mutableListOf<Double>(1.05, 1.19)),
            ChannelRelayTemplateAdjuster(mutableMapOf<String, Double>("north" to 1.23)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 89285.8,
    )
}
